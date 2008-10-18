package littleware.base;

import com.google.inject.*;

import com.google.inject.name.Names;
import java.util.*;
import java.io.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Guice module that sets Named attribute Guice constants corresponding
 * to the property values loaded from a properties file.
 * Also provides a static utility for loading and overlaying properties files
 * along a littleware specific search path starting with the classpath,
 * and progressing through littleware.home, user.home, etc. based locations.
 */
public class PropertiesLoader implements Module {

    private static Logger olog = Logger.getLogger( PropertiesLoader.class.getName() );
    private static Map<String, Properties> ov_cache = new HashMap<String, Properties>();
    
    private String os_littlehome_key = "littleware.home";
    
    private  File ofile_home = null;
    
    {
        try {
            String s_home = System.getProperty( os_littlehome_key );
            if ( null == s_home ) {
                s_home = System.getProperty ( "user.home" );
                if ( null != s_home ) {
                    s_home += "/.littleware";
                }
            }
            if ( null != s_home ) {
                File fh_home = new File( s_home );
                if ( fh_home.isDirectory() && fh_home.canRead() ) {
                    ofile_home = fh_home;
                } else if ( ! fh_home.exists() ) {
                    fh_home.mkdirs();
                    ofile_home = fh_home;
                }
            }
        } catch ( Exception ex ) {
            olog.log( Level.WARNING, "Unable to access littleware.home", ex );
        }
    }
    
    /**
     * Return System.getProperty( littleware.home ),
     * else System.getProperty( user.home ) + ".littleware",
     * else null
     * 
     * @return File referencing littleware home directory, or
     *           null if unable to locate and create
     */
    public File getLittleHome () {
        return ofile_home;
    }
    
    /**
     * Try to safely save the properties to the given file
     * by first writing to a temp file, then doing a couple renames.
     * 
     * @param props to save
     * @param fh_output destination
     * @throws java.io.IOException
     */
    public void safelySave( Properties props, File fh_output ) throws IOException {
        if ( fh_output.exists () ) {
            if ( fh_output.isDirectory() ) {
                throw new IOException( "Properties file is a directory: " + fh_output );
            }
        }
        File fh_parent = fh_output.getParentFile();
        File fh_temp = File.createTempFile( fh_output.getName(), null, fh_parent );
        FileOutputStream ostream = new FileOutputStream( fh_temp );
        try {
            props.store(ostream, "update form littleware" );
        } finally {
            ostream.close ();
        }
        File fh_safety = new File( fh_parent, 
                          fh_output.getName() + "." + new Date().getTime() 
                          );

        if ( fh_output.exists () ) {
            fh_output.renameTo( fh_safety );
        }
        fh_temp.renameTo( fh_output );
        fh_safety.delete();
    }
    
    /**
     * Get the default littleware properties file name
     * 
     * @return "littleware.properties"
     */
    public String  getDefaultProps () {
        return "littleware.properties";
    }
    
    private final Properties  oprop;
    
    
    private static PropertiesLoader  oloader_default = null;
    
    /**
     * Get the default PropertiesLoader
     */
    public static final PropertiesLoader get () {
        if ( null != oloader_default ) {
            return oloader_default;
        }
        synchronized ( olog ) {
            if ( null != oloader_default ) {
                return oloader_default;
            }

            try {
                oloader_default = new PropertiesLoader ();
                return oloader_default;
            } catch ( IOException ex ) {
                throw new AssertionFailedException( "Failed to initialize properties loader", ex );
            }
        }
    }
    
    /**
     * Shortcut for new PropoerteisLoader( PropertiesLoader.loadProperties( PropertiesLoader.getLittleProps () )
     */
    private PropertiesLoader () throws IOException {
        oprop = loadProperties( getDefaultProps () );
    }
    
    
    

    /** 
     * Configure the GUICE binding String constants according
     * to the constructor injected Properties.
     */
    public void configure( Binder binder_in ) {
        for( String s_key : oprop.stringPropertyNames() ) {
            binder_in.bindConstant().annotatedWith( Names.named(s_key)).to( oprop.getProperty(s_key));
        }
    }
 
    /**
     * If fh exists and is ledgible, then load its properties into prop_target,
     * otherwise NOOP
     * 
     * @param prop_target
     * @param fh
     */
    private void loadPropsFromFile( Properties prop_target, File fh ) throws IOException {
        try {
            if ( fh.isFile() && fh.canRead() ) {
                InputStream istream = new FileInputStream ( fh );
                try {
                    prop_target.load( istream );
                } finally {
                    istream.close ();
                }
            }
        } catch ( SecurityException ex ) {
            olog.log ( Level.WARNING, "Insufficient privileges to scan prop file: " + fh, ex );
        }
    }
    
    /**
     * Attempt to load the properties file with the given basename
     * if the codebase has permission to access the necessary files.
     * Silently log security exceptions, and continue on if possible.
     * Overlay properties from the following files
     *
     * <ol>
     *   <li> Properties file in the classpath </li>
     *   <li> If that system property does not exist,
     *           then next check if the file exists under
     *           the directory referenced by the littleware.home system property
     *          or user.home/littleware.home if littleware.home is not set
     *        </li>
     *   <li> Check if a System property exists with that
     *             name, and use that system property as the path
     *             to the properties file if it exists.
     *           If the System property is not set, then check
     *           the current directory.
     *          </li>
     * </ol>
     * If the file does not exist, then just return a new Properties
     * instance with the supplied defaults.
     * If the file exists, and the file load fails, then log the failure,
     * and propagate the IO exception.
     * Cache the returned properties before returning - subsequent calls
     * requesting the same name get the same Properties.
     * Prevent unauthorized code from loading config files by setting
     * file-access security policies on the directories where the properties live.
     *
     * @param s_name should be file basename and System property override
     * @param prop_defaults with needed defaults - ignored if pulled from cache
     * @return loaded (if possible) + defaults properties or cache under s_name
     * @exception IOException if file exists, but load fails
     */
    public synchronized Properties loadProperties(String s_name ) throws IOException {
        Properties prop_filedata = ov_cache.get(s_name);

        if (null != prop_filedata) {
            return (Properties) prop_filedata.clone ();
        }
        prop_filedata = new Properties();
        ov_cache.put( s_name, prop_filedata );
        {
            InputStream istream = PropertiesLoader.class.getClassLoader().getResourceAsStream(s_name);
            if ( null != istream ) {
                try {
                    prop_filedata.load(istream);
                } finally {
                    istream.close ();
                }
            }
        }
        File fh_home = getLittleHome ();
        if ( null != fh_home ) {
            loadPropsFromFile( prop_filedata, new File( fh_home, s_name ) );
        }
        try {
            String s_path = System.getProperty(s_name);
            if ( null != s_path ) {
                loadPropsFromFile( prop_filedata, new File( s_path ) );
            }
        } catch (SecurityException e) {
            olog.log(Level.INFO, "Insufficient privileges to access System property: " + s_name, e );
        }

        return (Properties) prop_filedata.clone ();        
    }

    /**
     * Shortcut for loadProperties( getDefaultProps )
     * @throws java.io.IOException
     */
    public Properties loadProperties () throws IOException {
        return loadProperties( getDefaultProps () );
    }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com
