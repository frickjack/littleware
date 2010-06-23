package littleware.web.beans;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.base.AssertionFailedException;
import littleware.base.PropertiesLoader;

/**
 * Stupid little bean that just gives callers
 * access to a map of key-value pairs initialized
 * out of the littleware.properties properties file.
 * Used to have a Map registered as an application-scope
 * bean with JSF, but the bean-initialization garbage
 * stopped working for some reason.
 */
public class DefaultsBean {
    private final static Logger               olog_generic = Logger.getLogger ( "littleware.web.beans.DefaultsBean" );
    private static Map<String,String>         ov_defaults = new HashMap<String,String> ();
    private static boolean                    ob_initialized = false;
    
    
    /**
     * Assemble info on application defaults in a single
     * synchronized setup.  Only does something the first
     * time called, subsequently is NOOP.
     */
    private static synchronized void initialize () {
        if ( ! ob_initialized ) {
            try {
                Properties      prop_littleware = PropertiesLoader.get().loadProperties ( );
                    
                ov_defaults.put ( "contact_email", prop_littleware.getProperty ( "web.info.email" ) );
                ov_defaults.put ( "serverName", prop_littleware.getProperty ( "web.hostname" ) );
            } catch ( RuntimeException e ) {
                throw e;
            } catch ( Exception e ) {
                throw new AssertionFailedException ( "Failed to setup Bean defaults", e );
            }
            ob_initialized = true;
            ov_defaults = Collections.unmodifiableMap( ov_defaults );
        }
    }
                
    public DefaultsBean () {
        initialize ();
    }
    
    /**
     * Get a reference to the umodifiable defaults map.
     */
    public Map<String,String> getDefaults () {
        return ov_defaults;
    }
    
}
