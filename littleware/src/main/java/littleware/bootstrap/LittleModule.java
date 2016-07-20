package littleware.bootstrap;

import com.google.inject.Module;
import java.util.Optional;


/**
 * Base littleware module interface defines Guice injection
 * module, and an optional OSGi activator to inject and
 * activate into the littleware runtime.
 */
public interface LittleModule extends Module {
    
    /**
     * Callback handler interface.
     * Implementations are allocated by the bootstrap runtime via guice injection
     * in arbitrary order, but satisfying guice constraints.
     */
    public interface LifecycleCallback {
        /**
         * Called at startUp time - callbacks to different
         * modules run in arbitrary order
         */
        void startUp();
        /**
         * Called at shutdown time - callbacks run in
         * arbitrary order
         */
        void shutDown();
    }
    
    /**
     * Return an optional class to inject; startUp() runs after all modules
     * have been configured, and shutDown() runs at LittleBootstrap.shutdown time.
     * Note the callback methods run inline with startup and shutdown
     */
    Optional<? extends Class<? extends LifecycleCallback>> getCallback();
}
