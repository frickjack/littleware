package littleware.base;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Test fixture for XmlResourceBundle
 */
public class XmlResourceBundleTester {
    private static final Logger log = Logger.getLogger( XmlResourceBundleTester.class.getName () );
    
    
    
    @Test
    public void testBasicXmlBundle() {
        final List<String> pathList = XmlResourceBundle.getResourcePaths( "foo.Bar", 
                new Locale( "en", "US" ) 
                );
        assertTrue( "XmlResourceBundle.getResourcePaths got some paths",
                ! pathList.isEmpty()
                );
        final Set<String> expectedSet = new HashSet<>(
                Arrays.asList( new String[]{
                        "foo/Bar", "foo/Bar_en", "foo/Bar_en_US"
                        }
        )
        );
        
        for( String path : pathList ) {
            assertTrue( "Path was expected: " + path,
                    expectedSet.contains( path ) );
        }
    }
}
