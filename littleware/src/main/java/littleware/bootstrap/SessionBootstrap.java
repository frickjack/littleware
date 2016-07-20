package littleware.bootstrap;

import java.util.Collection;
import java.util.UUID;


/**
 * Bootstrap a littleware session.
 * <ul>
 * <li> First, launch a standard AppBootstrap application </li>
 * <li> Next, inject a SessionBootstrap.SessionBuilder to setup one or more sessions </li>
 * </ul>
 */
public interface SessionBootstrap {

    
    public interface SessionBuilder {
        public Collection<SessionModuleFactory> getSessionModuleSet();
        public SessionBuilder addModuleFactory(SessionModuleFactory factory);
        public SessionBuilder removeModuleFactory(SessionModuleFactory factory);

        public UUID  getSessionId();
        public void  setSessionId( UUID v );
        public SessionBuilder sessionId( UUID v );
        
        public SessionBootstrap build();
        
    }

    /**
     * Read-only view of registered session modules
     */
    public Collection<SessionModule> getSessionModuleSet();
    
    /**
     * Globally unique id string
     */
    public UUID getSessionId();


    /**
     * Start a new session with access to session-specific services
     * like SessionManager and LoginManager.
     * Note that this method configures an unauthenticated session -
     * many littleware services require an authenticated session.
     * Register a listener with the littleAsset KeyChain to authenticate on demand.
     * May only invoke this method once.
     *
     * @param clazz the class to instantiate and inject
     * @return session-injected instance of clazz for an unauthenticated session
     * @exception IllegalStateException if bootstrap has not yet been called
     */
    public <T> T  startSession( Class<T> clazz );

}
