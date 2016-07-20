package littleware.base;


import java.util.logging.Logger;
import javax.security.auth.login.LoginContext;
import javax.security.auth.Subject;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import littleware.base.login.LoginCallbackHandler;
import littleware.base.login.PropertiesLoginModule;
import littleware.test.LittleTest;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Test the littleware.base.login.PropertiesLoginModule
 */
public class PropertiesLoginTester  {
   
    private static final Logger log = Logger.getLogger( PropertiesLoginTester.class.getName() );
    private static final Configuration testLoginConfig;
    static {
        final java.util.Map<String,String> empty = java.util.Collections.emptyMap();
        final AppConfigurationEntry[] entry = {new AppConfigurationEntry(PropertiesLoginModule.class.getName(), AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, empty )};
        testLoginConfig = new Configuration() {

            @Override
            public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
                return entry;
            }
        };
    }
    
    @Test
    public void testLogin() {
        try {
            final Subject subject = new Subject();
            final String name = "littleware.test_user";
            final LoginContext ctx = new LoginContext( "testLogin", subject, new LoginCallbackHandler( name, "test123"), testLoginConfig );
            ctx.login();
            // login succeeded!
            assertTrue( "test login went ok!", 
                    subject.getPrincipals().iterator().next().getName().equals( name ) 
                    );
        } catch ( Exception ex ) {
            LittleTest.handle(ex);
        }
    }
    
}
