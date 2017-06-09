package littleware.apps.fishRunner.test;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.google.inject.Binder;
import java.io.File;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;
import littleware.bootstrap.AppBootstrap;
import littleware.bootstrap.AppModule;
import littleware.bootstrap.AppModuleFactory;
import littleware.bootstrap.helper.AbstractAppModule;

/**
 * Setup and run through all the tests ...
 */
public class LittleModule extends AbstractAppModule {
    private static final Logger log = Logger.getLogger(LittleModule.class.getName() );

    public static class AppFactory implements AppModuleFactory {
        @Override
        public AppModule build(AppBootstrap.AppProfile profile) {
            return new LittleModule(profile);
        }
    }
    
    
    public LittleModule(AppBootstrap.AppProfile profile) {
        super(profile);
    }
    

    @Override
    public void configure(Binder binder) {
        try {
            // TODO - load config variables from properties file ... or do it in FishModule ?
            final String propsPath = LittleModule.class.getName().replaceAll( "\\.+", "/" ) + ".properties";
            final InputStream propStream = LittleModule.class.getClassLoader().getResourceAsStream( propsPath );
            final Properties  props = new Properties();
            if ( null != propStream ) {
                try {
                    props.load(propStream);
                } finally { propStream.close(); }
            }
            /*
            Assert.assertTrue( "Required properties found in resource: " + propsPath, 
                    props.containsKey( "test.war" ) &&
                    props.containsKey( "test.s3creds" ) &&
                    props.containsKey( "test.dbURI" )
                    );
*/
            final AWSCredentials creds = new PropertiesCredentials( new java.io.File( props.getProperty("test.s3creds")) );
            binder.bind( FishCase.TestConfig.class 
                                    ).toInstance( new FishCase.TestConfig( new File( props.getProperty( "test.war" )) ) 
                                    );
                        
            
        } catch (Exception ex) {
            throw new RuntimeException( "Failed test setup", ex );
        }
    }

}
