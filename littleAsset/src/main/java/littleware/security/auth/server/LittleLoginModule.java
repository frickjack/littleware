package littleware.security.auth.server;

import com.google.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import littleware.asset.Asset;
import littleware.asset.server.LittleContext;
import littleware.asset.server.ServerSearchManager;
import littleware.base.Whatever;
import littleware.security.LittleUser;

/**
 * Login module tests a SHA256 hash of the password against the
 * password hash stored with a LittleUser.  Failes if the password
 * is empty or if the user does not already exist.  Relies on the 
 * AuthModuleFactory to inject the littleware tools the module needs
 * to access the asset repository at startup time via the injectTools
 * static method.
 */
public class LittleLoginModule implements LoginModule {
    private static final Logger log = Logger.getLogger( LittleLoginModule.class.getName() );
    
    private Subject subject = null;
    private CallbackHandler callbackHandler = null;
    
    public static class Tools {
        private final ServerSearchManager search;
        private final LittleContext.ContextFactory ctxFactory;
        
        @Inject
        public Tools( ServerSearchManager search, LittleContext.ContextFactory ctxFactory ) {
            this.search = search;
            this.ctxFactory = ctxFactory;
        }
        
        public ServerSearchManager getSearch() { return search; }
        public LittleContext.ContextFactory getCtxFactory() { return ctxFactory; }
    }
    
    private static Tools tools = null;
    
    /**
     * Little utility shared by both LittleLoginModule and LocalTrustLoginModule
     * 
     * @throws FailedLoginException on failure to access the repository
     */
    static Optional<LittleUser> lookupUser( String userName ) throws FailedLoginException {
        if ( null == tools ) {
            throw new IllegalStateException( "Tools net yet initialized" );
        }
        final LittleContext ctx = tools.getCtxFactory().buildAdminContext();
        ctx.getTransaction().startDbAccess();
        
        try {
            final Optional<Asset> maybe = tools.search.getByName( ctx, userName, LittleUser.USER_TYPE );
            if ( ! maybe.isPresent() ) {
                return Optional.empty();
            }
            final LittleUser user = maybe.get().narrow();
            if ( ! user.getStatus().equals( LittleUser.Status.ACTIVE ) ) {
                return Optional.empty();
            }
            return Optional.ofNullable( user );
        } catch ( Exception ex ) {
            log.log( Level.WARNING, "Failed asset lookup", ex );
            throw new FailedLoginException( "Failed repository lookup" );
        } finally {
            ctx.getTransaction().endDbAccess();
        }
        
    }
    
    public static void  injectTools( Tools value ) {
        tools = value;
    }

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    @Override
    public boolean login() throws LoginException {
        if (null == callbackHandler) {
            throw new LoginException("No CallbackHandler registered with module");
        }
        if (null == subject) {
            throw new LoginException("Subject never setup");
        }
        if ( null == tools ) {
            throw new LoginException( "Tools not injected to LoginModule" );
        }

        final String userName;
        final String password;

        try {
            // Collect username and password via callbacks
            final Callback[] callbacks = {
                new NameCallback("Enter username", "nobody"),
                new PasswordCallback("Enter password", false)
            };
            callbackHandler.handle(callbacks);

            userName = ((NameCallback) callbacks[ 0]).getName();
            password = new String(((PasswordCallback) callbacks[ 1]).getPassword());

        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw (LoginException) (new LoginException("Failure handling callbacks")).initCause(ex);
        }

        if ( Whatever.get().empty( password ) || Whatever.get().empty( userName ) || userName.equals( "nobody" ) ) {
            throw new FailedLoginException();
        }
        
        final LittleUser user;
        {
            final Optional<LittleUser> maybe = lookupUser( userName );
            if ( ! maybe.isPresent() ) {
                throw new FailedLoginException();
            }
            user = maybe.get();
        }
        if( user.testPassword(password) ) {
            return true;
        }
        throw new FailedLoginException();
    }

    @Override
    public boolean commit() throws LoginException {
        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        return true;
    }
}
