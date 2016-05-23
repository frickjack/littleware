/*
 * Copyright 2008, Reuben Pasquini
 * All Rights Reserved except as specified in license
 */

package littleware.base.test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;

import junit.framework.*;

import littleware.base.XmlResourceBundle;

/**
 * Test fixture for XmlResourceBundle
 */
public class XmlResourceBundleTester extends TestCase {
    private static Logger olog = Logger.getLogger( XmlResourceBundleTester.class.getName () );
    
    /**
     * Inject test name
     * @param s_name
     */
    public XmlResourceBundleTester( String s_name ) {
        super( s_name );
    }
    
    @Override
    public void setUp () {}
    @Override
    public void tearDown () {}
    
    public void testBasicXmlBundle() {
        final List<String> v_paths = XmlResourceBundle.getResourcePaths( "foo.Bar", 
                new Locale( "en", "US" ) 
                );
        assertTrue( "XmlResourceBundle.getResourcePaths got some paths",
                ! v_paths.isEmpty()
                );
        final Set<String> v_expect = new HashSet<String>(
                Arrays.asList( new String[]{
                        "/foo/Bar", "/foo/Bar_en", "/foo/Bar_en_US"
                        }
        )
        );
        
        for( String s_path : v_paths ) {
            assertTrue( "Path was expected: " + s_path,
                    v_paths.contains( s_path ) );
        }
    }
}
