/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.base;

import com.google.inject.ProvidedBy;
import com.google.inject.Provider;
import com.google.inject.Singleton;
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
@Singleton
@ProvidedBy(PropertiesLoader.ProvideLoader.class)
public class PropertiesLoader {

    /**
     * Allow PropertiesLoader GUICE injection
     */
    public static class ProvideLoader implements Provider<PropertiesLoader> {

        @Override
        public PropertiesLoader get() {
            return PropertiesLoader.get();
        }

    }

    private static final Logger log = Logger.getLogger( PropertiesLoader.class.getName() );
    private static final Map<String, Properties> cache = new HashMap<String, Properties>();
    private static final String LITTLEHOME = "littleware.home";
    
    private  final Maybe<File> maybeHome;
    private final Properties  defaultProperties;


    {
        Maybe<File> maybe = Maybe.empty();
        try {
            String sHome = System.getProperty( LITTLEHOME );
            if ( null == sHome ) {
                sHome = System.getProperty ( "user.home" );
                if ( null != sHome ) {
                    sHome += "/.littleware";
                }
            }
            if ( null != sHome ) {
                File fh_home = new File( sHome );
                if ( fh_home.isDirectory() && fh_home.canRead() ) {
                    maybe = Maybe.something( fh_home );
                } else if ( ! fh_home.exists() ) {
                    fh_home.mkdirs();
                    maybe = Maybe.something( fh_home );
                }
            }
        } catch ( Exception ex ) {
            log.log( Level.WARNING, "Unable to access littleware.home", ex );
        } finally {
            maybeHome = maybe;
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
    public Maybe<File> getLittleHome () {
        return maybeHome;
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
        final File fh_parent = fh_output.getParentFile();
        final File fh_temp = File.createTempFile( fh_output.getName(), null, fh_parent );
        final FileOutputStream ostream = new FileOutputStream( fh_temp );
        try {
            props.store(ostream, "update form littleware" );
        } finally {
            ostream.close ();
        }
        final File fh_safety = new File( fh_parent,
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
    
    
    
    private static PropertiesLoader  singleton = null;
    
    /**
     * Get the default PropertiesLoader
     */
    public static final PropertiesLoader get () {
        if ( null != singleton ) {
            return singleton;
        }
        synchronized ( log ) {
            if ( null != singleton ) {
                return singleton;
            }

            try {
                singleton = new PropertiesLoader ();
                return singleton;
            } catch ( IOException ex ) {
                throw new AssertionFailedException( "Failed to initialize properties loader", ex );
            }
        }
    }
    
    /**
     * Shortcut for new PropoerteisLoader( PropertiesLoader.loadProperties( PropertiesLoader.getLittleProps () )
     */
    private PropertiesLoader () throws IOException {
        defaultProperties = loadProperties( getDefaultProps () );
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
                    log.log( Level.INFO, "Loading " + fh );
                } finally {
                    istream.close ();
                }
            }
        } catch ( SecurityException ex ) {
            log.log ( Level.WARNING, "Insufficient privileges to scan prop file: " + fh, ex );
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
        Properties prop_filedata = cache.get(s_name);

        if (null != prop_filedata) {
            return (Properties) prop_filedata.clone ();
        }
        prop_filedata = new Properties();
        cache.put( s_name, prop_filedata );
        {
            final InputStream istream = PropertiesLoader.class.getClassLoader().getResourceAsStream(s_name);
            if ( null != istream ) {
                try {
                    prop_filedata.load(istream);
                } finally {
                    istream.close ();
                }
            }
        }

        if ( maybeHome.isSet() ) {
            loadPropsFromFile( prop_filedata,  new File( maybeHome.get(), s_name ) );
        }
        try {
            String s_path = System.getProperty(s_name);
            if ( null != s_path ) {
                loadPropsFromFile( prop_filedata, new File( s_path ) );
            } 
        } catch (SecurityException e) {
            log.log(Level.WARNING, "Insufficient privileges to access System property: " + s_name, e );
        }

        return (Properties) prop_filedata.clone ();        
    }
    
    
    /**
     * Littleware server still has a ResourceBundle based pull-injection 
     * mechanism.  We want to migrate the server to Guice (like the client),
     * but also want to look into OSGi, etc.  Turns out the glassfish
     * container ResourceBundle resolver is not finding our "bundle" classes,
     * so we use this little bundle loader as a workaround for now until
     * we can refactor the server bootstrap to a better system.
     * 
     * @param s_bundle_class class name
     * @return new instance of requested RequestBundle class
     */
    public ResourceBundle getBundle( String s_bundle_class ) {
        try {
            return (ResourceBundle) Class.forName( s_bundle_class ).newInstance();
        } catch ( RuntimeException ex ) {            
            throw ex;
        } catch ( Exception ex ) {
            throw new IllegalArgumentException( "Unable to load ResourceBundle as class: " + s_bundle_class, ex );
        }
    }

    /**
     * Shortcut for loadProperties( getDefaultProps )
     * @throws java.io.IOException
     */
    public Properties loadProperties () throws IOException {
        return loadProperties( getDefaultProps () );
    }

    /**
     * Utility simplifies process of saving application-specific
     * settings in an application level properties file.
     *
     * @param in default properties
     * @param overrides override properties
     * @return in.clone with properties in both in and overrides replaced
     *                with the overrides properties
     */
    public Properties applyOverrides( Properties in, Properties overrides ) {
        final Properties result = (Properties) in.clone();
        for( Object key : overrides.keySet() ) {
            if ( in.containsKey(key) ) {
                result.setProperty( (String) key, (String) overrides.get(key) );
            }
        }
        return result;
    }
}

