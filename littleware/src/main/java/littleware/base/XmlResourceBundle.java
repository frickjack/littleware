package littleware.base;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Properties-based resource bundle rigged up to
 * load from XML formated properties file.
 */
public class XmlResourceBundle extends ResourceBundle {
     private final static Logger  log = Logger.getLogger( XmlResourceBundle.class.getName()  );
     private final Properties oprops;
     
     /**
      * Load resource bundle from stream pointing to XML prop file
      * 
      * @param stream referencing XML properties file
      * @throws java.io.IOException
      */
     public XmlResourceBundle(InputStream stream) throws IOException {
         oprops = new Properties();
         oprops.loadFromXML(stream);
     }
     
     public XmlResourceBundle( Properties props ) {
         oprops = props;
     }

    @Override
     protected Object handleGetObject(String key) {
         return oprops.getProperty(key);
     }

    @Override
    public Enumeration<String> getKeys() {
        Set<String> v_keys = new HashSet<String> ();

        for( Object x_key : oprops.keySet() ) {
            v_keys.add( (String) x_key );
        }
        if( null != parent ) {
            v_keys.addAll ( parent.keySet () );
        }
        return Collections.enumeration( v_keys );
    }
    
    /**
     * Calls XmlResourceBundle.getBundle( s_basename, 
     *                                    locale.getDefault, 
     *                                    ClassLoader.getSystemClassLoader
     * );
     * 
     * @throws MissingResourceException on failure
     */
    public static ResourceBundle getXmlBundle ( String s_basename ) 
    {
        return XmlResourceBundle.getXmlBundle( s_basename, Locale.getDefault (), 
                                                ClassLoader.getSystemClassLoader() );
    }
    /**
     * Calls XmlResourceBundle.getXmlBundle( s_basename, 
     *                                    locale, ClassLoader.getSystemClassLoader
     *                                    
     *      );
     * 
     * @throws MissingResourceException on failure
     */
    public static ResourceBundle getXmlBundle ( String s_basename, Locale locale ) 
    {
        return XmlResourceBundle.getXmlBundle( s_basename, locale, ClassLoader.getSystemClassLoader() );
    }

    private static Map<String,ResourceBundle>  omap_cache =
            Collections.synchronizedMap( new HashMap<String,ResourceBundle> () );
    
    /**
     * Look for an XML file along
     * the paths returned by getResourcePaths,
     * and load up an XmlResourceBundle if found,
     * otherwise throw MissingResourceException.
     * 
     * @param s_basename of resource to lookup
     * @param locale of resource 
     * @param classloader to search with 
     * @throws MissingResourceException on failure
     * @throws NullPoitnerException if locale is null or classloader is null
     */
    public static ResourceBundle getXmlBundle ( String s_basename,
            Locale locale, 
            ClassLoader classloader
            ) 
    {                
        if ( null == locale ) {
            throw new NullPointerException( "null locale" );
        }
        if ( null == classloader ) {
            throw new NullPointerException( "null classloader" );
        } 
        String s_cachekey = s_basename 
                + "_" + locale.getLanguage() 
                + "_" + locale.getCountry () 
                + "_" + locale.getVariant ();
        ResourceBundle bundle_result = omap_cache.get( s_cachekey );
        if ( null != bundle_result ) {
            return bundle_result;
        }
        
        for( String s_search : getResourcePaths( s_basename, locale ) ) {
            // first try to load the class     
            InputStream istream = classloader.getResourceAsStream(s_search + ".xml");
            if (null != istream) {
                try {
                    bundle_result = new PropertyResourceBundle(istream);
                    break;
                } catch (Exception e) {
                    log.log(Level.WARNING, "Failed to load: " + s_search + ".xml, caught: " + e);
                } finally {
                    try {
                        istream.close();
                    } catch( IOException e ) {}
                }
            }            
        }
        omap_cache.put(s_cachekey, bundle_result);
        return bundle_result;        
    }
    
    /**
     * Return the paths along which to source for the given resource.
     * Caller probably needs to add an extention if looking for a file.
     * Ex: getResourcePaths( "foo.Bar", new Locale( "en", "US" ), "POSIX" );
     * returns:
     *     [ "foo/Bar_en_US_POSIX", "foo/Bar_en_US", "foo/Bar_en", "foo/Bar" ]
     * 
     * @param baseName class to get search paths for
     * @param locale defaults to Locale.getDefault if null
     * @return search paths
     */
    public static List<String> getResourcePaths( String baseName, Locale locale ) {
        final List<String> v_result = new ArrayList<> ();
        
        if ( null == locale ) {
            locale = Locale.getDefault();
        }
        final String variant = locale.getVariant ();
        final List<String> tokenList = new ArrayList<>();
        tokenList.addAll (
            Arrays.asList( new String[]{
                baseName.replaceAll("(\\.|/)+", "/" ), 
                "_" + locale.getLanguage(), 
                "_" + locale.getCountry()
            }
            )
            );
        if ( null != variant && ! variant.isEmpty() ) {
            tokenList.add ( "_" + variant );
        }
        for( int i=0; i < tokenList.size(); ++i ) {
            String entryPath = tokenList.get( 0 );
            for ( int j=1; j < tokenList.size () - i; ++j ) {
                entryPath += tokenList.get( j );                
            }
            v_result.add( entryPath );
        }
        return v_result;
    }
    
    
}