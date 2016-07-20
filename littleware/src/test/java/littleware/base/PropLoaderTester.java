package littleware.base;

import java.util.Properties;
import java.util.logging.Logger;
import littleware.test.LittleTest;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author pasquini
 */
public class PropLoaderTester {
    private final Logger log = Logger.getLogger( PropLoaderTester.class.getName() );

    
    @Test
    public void testPropLoader() {
        final PropertiesLoader loader = PropertiesLoader.get();
        try {
            final String path = loader.classToResourcePath(getClass() );
            assertTrue( "Got expected resource path:" + path,
                    path.equals( "littleware/base/test/PropLoaderTester.properties" )
                    );
            final Properties props = loader.loadProperties( getClass() );
            assertTrue( "Test property set has one member", props.size() == 1 );
            final String testProp1 = props.getProperty( "testProp1", "ugh!" );
            assertTrue( "Property has expected value: " + testProp1,
                    testProp1.equals( "blaBlaBla" )
                    );
        } catch ( Exception ex ) {
            LittleTest.handle(ex);
        }
    }
}
