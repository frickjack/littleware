package littleware.base;

import java.util.*;
import java.security.*;
import java.io.*;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Just provide a utility for loading properties files
 */
public class PropertiesLoader {

    private static Logger olog_generic = Logger.getLogger("littleware.base.PropertiesLoader");
    private static Map<String, Properties> ov_cache = new HashMap<String, Properties>();

    /**
     * Attempt to load the properties file with the given basename
     * if the codebase has permission to access the necessary files.
     * Silently log security exceptions, and continue on if possible.
     *
     * <ol>
     *   <li> First - check if a System property exists with that
     *             name, and use that system property as the path
     *             to the properties file if it exists
     *              </li>
     *   <li> If that system property does not exist,
     *           then next check if the file exists under
     *           the directory referenced by the littleware.install system property
     *        </li>
     * <li> Next check if the properties file is saved in the classpath,
     *      and can be loaded as a ResourceBundle. </li>
     * <li> Finally - check the current directory for the file </li>
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
    public static synchronized Properties loadProperties(String s_name, Properties prop_defaults) throws IOException {
        Properties prop_filedata = ov_cache.get(s_name);

        if (null == prop_filedata) {
            prop_filedata = new Properties();
            ov_cache.put(s_name, prop_filedata);
            String s_path = null;

            try {
                s_path = System.getProperty(s_name);
            } catch (SecurityException e) {
                olog_generic.log(Level.INFO, "Insufficient privileges to access System property: " + s_name + ", caught: " + e);
            }

            if (null == s_path) {
                String s_workdir = null;
                try {
                    s_workdir = System.getProperty("littleware.install");
                } catch (SecurityException e) {
                    olog_generic.log(Level.INFO, "Insufficient privileges to access System property: littleware.install, caught: " + e);
                }
                if (null != s_workdir) {
                    s_path = s_workdir + "/" + s_name;
                    File fh_props = new File(s_path);
                    if (!fh_props.exists()) {
                        olog_generic.log(Level.FINE, "littleware.install path does not exist for " + fh_props.getAbsolutePath());
                        s_path = s_name;
                    }
                } else if ( s_name.endsWith( ".properties" ) ) {
                    olog_generic.log ( Level.FINE, "Trying to load as resource bundle: " + s_name );
                    String s_resource = s_name.substring( 0, s_name.lastIndexOf( ".properties" ) );
                    try {
                        ResourceBundle bundle_in = ResourceBundle.getBundle( s_resource );
                        
                        Enumeration<String>  v_keys = bundle_in.getKeys ();
                        if ( v_keys.hasMoreElements() ) {
                            while ( v_keys.hasMoreElements () ) {
				String s_key = v_keys.nextElement ();
                                prop_filedata.setProperty( s_key, (String) bundle_in.getObject( s_key ) );
                            }
                            return prop_filedata;
                        }
                    } catch ( Exception e ) {
                        olog_generic.log ( Level.FINE, "Unable to load as resource bundle: " + s_name );
                        s_path = s_name;
                    }
                } else {                    
                    olog_generic.log(Level.FINE, "littleware.install system property not set");
                    s_path = s_name;
                }
            }
            try {
                File fh_props = new File(s_path);
                if (fh_props.exists()) {
                    InputStream io_props = null;
                    try {
                        io_props = new FileInputStream(fh_props);
                        prop_filedata.load(io_props);
                    } catch (IOException e) {
                        olog_generic.log(Level.WARNING, "Failure loading properties file: " + s_path + ", caught: " + e);
                        throw e;
                    } finally {
                        if (null != io_props) {
                            try {
                                io_props.close();
                            } catch (Exception e) {
                            }
                        }
                    }
                } else {
                    olog_generic.log(Level.INFO, "Not loading properties file that does not exist: " + s_path);
                }
            } catch (SecurityException e) {
                olog_generic.log(Level.WARNING, "Security constrained environment, may not access: " + s_path + ", caught: " + e);
            }
        }

        Properties prop_result = new Properties(prop_defaults);
        for (Enumeration r_i = prop_filedata.propertyNames(); r_i.hasMoreElements();) {
            String s_key = (String) r_i.nextElement ();
            prop_result.put(s_key, prop_filedata.get(s_key));
        }
        return prop_result;
    }
}
// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com
