/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package littleware.base;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
     private final static Logger  olog = Logger.getLogger( XmlResourceBundle.class.getName()  );
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

     protected Object handleGetObject(String key) {
         return oprops.getProperty(key);
     }

    @Override
    public Enumeration<String> getKeys() {
        if ( null == parent ) {            
            return Collections.enumeration( oprops.keySet() );
        } else {
            Set<String> v_keys = new HashSet( oprops.keySet() );
            for ( String s_key : parent.getKeys() ) {
                v_keys.add ( s_key );
            }
            return Collections.enumeration( v_keys );
        }
    }
    
    /**
     * Calls XmlResourceBundle.getBundle( s_basename, 
     *                                    locale.getDefault, 
     *                                    ClassLoader.getDefault
     * );
     * 
     * @exception MissingResourceException on failure
     */
    public static ResourceBundle getXmlBundle ( String s_basename ) 
    {
        return XmlResourceBundle.getXmlBundle( s_basename, null, null, null );
    }
    /**
     * Calls XmlResourceBundle.getXmlBundle( s_basename, 
     *                                    locale, ClassLoader.getDefault 
     *                                    
     *      );
     * 
     * @exception MissingResourceException on failure
     */
    public static ResourceBundle getXmlBundle ( String s_basename, Locale locale ) 
    {
        return XmlResourceBundle.getXmlBundle( s_basename, locale, null, null );    
    }

    private static Map<String,ResourceBundle>  omap_cache =
            Collections.synchronizedMap( new HashMap<String,ResourceBundle> () );
    
    /**
     * Look for an XML, properties, or ResourceBundle class file along
     * the paths returned by getResourcePaths,
     * and load up an XmlResourceBundle if found,
     * otherwise throw MissingResourceException.
     * Tries to setup parent relationship too.
     * Result gets cached on hit.
     * 
     * @param s_basename of resource to lookup
     * @param locale of resource - null indicates Locale.getDefault
     * @param s_variant of resource - ignored if null
     * @param classloader to search with - null indicates default loader
     * @exception MissingResourceException on failure
     */
    public static ResourceBundle getXmlBundle ( String s_basename,
            Locale locale, 
            ClassLoader classloader
            ) 
    {                
        if ( null == locale ) {
            locale = Locale.getDefault ();
        }
        if ( null == classloader ) {
            classloader = ClassLoader.getSystemClassLoader();
        } 
        String s_cachekey = s_basename 
                + "_" + locale.getLanguage() 
                + "_" + locale.getCountry () 
                + "_" + locale.getVariant ();
        ResourceBundle bundle_result = omap_cache.get( s_cachekey );
        if ( null != bundle_result ) {
            return bundle_result;
        }
        
        List<ResourceBundle> v_bundles = new ArrayList<ResourceBundle> ();
        for( String s_search : getResourcePaths( s_basename, locale ) ) {
            // first try to load the class
            ResourceBundle bundle_node = null;
            
            try {
                String s_class = s_search.replaceAll( "/", "." );
                if ( s_class.charAt(0) == '.' ) {
                    s_class = s_class.substring( 1 );
                }
                bundle_node = (ResourceBundle) classloader.loadClass( s_class ).newInstance();
                v_bundles.add( bundle_node );
                continue;
            } catch( Exception e ) {                
            }
            
            InputStream istream = classloader.getResourceAsStream(s_search + ".properties");
            if (null != istream) {
                try {
                    bundle_node = new PropertyResourceBundle(istream);
                    v_bundles.add(bundle_node);
                    continue;
                } catch (Exception e) {
                    olog.log(Level.WARNING, "Failed to load: " + s_search + ".properties, caught: " + e);
                } finally {
                    istream.close();
                }
            }
                        
            istream = classloader.getResourceAsStream(s_search + ".xml");
            if (null != istream) {
                try {
                    bundle_node = new PropertyResourceBundle(istream);
                    v_bundles.add(bundle_node);
                    continue;
                } catch (Exception e) {
                    olog.log(Level.WARNING, "Failed to load: " + s_search + ".xml, caught: " + e);
                } finally {
                    istream.close();
                }
            }            
        }
        if ( v_bundles.isEmpty () ) {
            throw new MissingResourceException( "No resources found",                      
                    s_basename, "" );
        }
        for ( int i=1; i < v_bundles.size (); ++i ) {
            v_bundles.get( i-1 ).parent = v_bundles.get( i );
        }
        bundle_result = v_bundles.get(0);
        omap_cache.put( s_cachekey, bundle_result );
        return bundle_result;
    }
    
    /**
     * Return the paths along which to source for the given resource.
     * Caller probably needs to add an extention if looking for a file.
     * Ex: getResourcePaths( "foo.Bar", new Locale( "en", "US" ), "POSIX" );
     * returns:
     *     [ "/foo/Bar_en_US_POSIX", "/foo/Bar_en_US", "/foo/Bar_en", "/foo/Bar" ]
     * 
     * @param s_basename class to get search paths for
     * @param locale defaults to Locale.getDefault if null
     * @param s_variant may be null
     * @return search paths
     */
    public static List<String> getResourcePaths( String s_basename, Locale locale ) {
        List<String> v_result = new ArrayList<String> ();
        
        if ( null == locale ) {
            locale = Locale.getDefault();
        }
        String s_variant = locale.getVariant ();
        List<String> v_tokens = 
            Arrays.asList( new String[]{
                s_basename, 
                "_" + locale.getLanguage(), 
                "_" + locale.getCountry()
            }
            );
        if ( null != s_variant ) {
            v_tokens.add ( "_" + s_variant );
        }
        for( int i=0; i < v_tokens.size(); ++i ) {
            String s_result = v_tokens.get( 0 );
            for ( int j=1; j < v_tokens.size () - i; ++j ) {
                s_result += v_tokens.get( j );                
            }
            v_result.add( s_result );
        }
        return v_result;
    }
    
    
}