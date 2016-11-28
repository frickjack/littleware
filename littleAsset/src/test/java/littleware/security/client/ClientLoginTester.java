package littleware.security.client;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import littleware.asset.client.AssetSearchManager;
import littleware.asset.client.test.AbstractAssetTest;
import littleware.security.auth.LittleSession;
import littleware.base.login.LoginCallbackHandler;
import littleware.security.LittleUser;
import littleware.security.auth.client.KeyChain;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test ClientLoginModule
 */
@RunWith(littleware.test.LittleTestRunner.class)
public class ClientLoginTester extends AbstractAssetTest {

    private static final Logger log = Logger.getLogger(ClientLoginTester.class.getName());
    private final Provider<javax.security.auth.login.Configuration> configProvider;
    private final KeyChain keychain;
    private final Provider<LittleUser.Builder> userFactory;
    private final AssetSearchManager search;

    @Inject
    public ClientLoginTester(Provider<Configuration> configProvider, KeyChain keychain,
            Provider<LittleUser.Builder> userFactory, AssetSearchManager search) {
        this.configProvider = configProvider;
        this.keychain = keychain;
        this.userFactory = userFactory;
        this.search = search;
    }

    public static class Listener implements PropertyChangeListener {

        public boolean eventFired = false;

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            assertTrue("Got expected event: " + evt.getPropertyName(), evt.getPropertyName().equals("defaultSessionId"));
            eventFired = true;
        }
    }

    @Test
    public void testClientLogin() {
        try {
            final LoginContext context = new LoginContext("littleware.login",
                    new Subject(),
                    new LoginCallbackHandler(AbstractAssetTest.getTestUserName(), "password"),
                    configProvider.get());
            final Listener listener = new Listener();
            try {
                keychain.addPropertyChangeListener(listener);
                context.login();
            } finally {
                keychain.removePropertyChangeListener(listener);
            }
            assertTrue("Keychain fired property change on fresh login", listener.eventFired);
            assertTrue("Authenticated subject includes LittleSession in credentials",
                    !context.getSubject().getPublicCredentials(LittleSession.class).isEmpty());
        } catch (Exception ex) {
            log.log(Level.WARNING, "Failed test", ex);
            fail("Caught exception: " + ex);
        }
    }

    /**
     * Test the user password thing
     */
    public void testUserPassword() {
        try {
            final LittleUser user = userFactory.get().parent(getTestHome(search)).name("bla").password("frickjack").build();
            assertTrue( "User has expected password", user.testPassword( "frickjack" ) );
            assertTrue( "User password is not bogus", ! user.testPassword( "FrickJack" ) );
            assertTrue( "Password gets copied ok", user.copy().build().testPassword( "frickjack" ) );
        } catch (Exception ex) {
            log.log(Level.WARNING, "Failed test", ex);
            fail("Caught exception: " + ex);
        }

    }
}
