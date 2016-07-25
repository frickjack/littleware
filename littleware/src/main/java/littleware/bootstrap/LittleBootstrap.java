package littleware.bootstrap;

import java.util.Collection;
import java.util.Optional;
import littleware.bootstrap.internal.SimpleAppFactory;

/**
 * Just a little interface that a bootstrap class
 * should implement.  
 */
public interface LittleBootstrap {
    /**
     * Bootstrap a littleware environment - assumes registered modules
     * launch processing
     */
    public void bootstrap ();

    /**
     * Boot the littleware runtime and return a guice-injected
     * instance of the given class.
     *
     * @param bootClass to instantiate
     * @return injected object upon system startup
     */
    public <T> T bootstrap( Class<T> bootClass );
    
    /**
     * Startup the application-scope runtime if not already done so, and instantiate
     * a SessionBootstrap.SessionBuilder object
     * 
     * @return builder with which to start a user session
     */
    public SessionBootstrap.SessionBuilder  newSessionBuilder();
    

    /**
     * Shutdown the littleware component associated with this object.
     */
    public void shutdown();

    /**
     * Bootstrap plugins
     */
    public Collection<? extends LittleModule> getModuleSet();

    public interface Builder {
        public LittleBootstrap build();
    }

    /**
     * Current runtime implementation requires that only one
     * application bootstrap at a time.
     * This Factory helps enforce that constraint, and provides
     * hooks for code outside the normal injection control-flow to
     * allocate instances of classes managed by littleware.
     */
    interface Factory {
        /**
         * Return the active app if littleware runtime has been bootstrap,
         * otherwise return false
         */
        Optional<LittleBootstrap> getActiveRuntime();
        
        /**
         * Lookup the given class in the active littleware runtime.
         * If littleware has not yet bootstrapped, then bootstrap the
         * default environment.
         * Note that this method cannot allocate session-scoped classes,
         * since it doesn't know which session the client code is associated with.
         */
        <T> T lookup( Class<T> clazz );
    }
    

    /**
     * Handle for global Factory singleton
     */
    public static final Factory factory = SimpleAppFactory.getSingleton();
}
