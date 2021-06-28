package littleware.bootstrap.helper;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.bootstrap.LittleBootstrap;
import littleware.bootstrap.LittleModule;
import littleware.bootstrap.internal.SimpleAppFactory;

public abstract class AbstractLittleBootstrap<T extends LittleModule> implements LittleBootstrap {

    private static final Logger log = Logger.getLogger(AbstractLittleBootstrap.class.getName());
    private final Collection<? extends T> moduleSet;

    protected AbstractLittleBootstrap(Collection<? extends T> moduleSet) {
        this.moduleSet = ImmutableList.copyOf(moduleSet);
    }

    @Override
    public Collection<? extends T> getModuleSet() {
        return moduleSet;
    }
    private boolean bootstrapDone = false;
    final List<LittleModule.LifecycleCallback> callbackList = new ArrayList<>();

    /**
     * Build application-scope root guice injector configured with moduleSet, and call startUp
     * on the LifecycleCallbacks associated with the modules in moduleSet.
     * Register the root injector with the
     * 
     * @param moduleSet modules to configure guice with
     * @return root injector
     */
    protected final Injector bootstrapCore( Collection<? extends T> moduleSet) {
        if (bootstrapDone) {
            throw new IllegalStateException("bootstrapCore can only run once");
        }
        final SimpleAppFactory appFactory = SimpleAppFactory.getSingleton();
        if ( appFactory.getActiveRuntime().isPresent() ) {
            throw new IllegalStateException( "Another littleware runtime is already active ..." );
        }
        bootstrapDone = true;

        // see https://github.com/google/guice/wiki/Guice501
        System.setProperty("guice_bytecode_gen_option", "DISABLED");
        
        for (T scan : moduleSet) {
            log.log(Level.FINE, "Check bootstrap module: {0}", scan.getClass().getName());
        }
        final Injector injector = Guice.createInjector(
                moduleSet);

        // Get Guice injected instances of the module lifecycle callbacks
        for (LittleModule module : moduleSet) {
            final Optional<? extends Class<? extends LittleModule.LifecycleCallback>> optCallback = module.getCallback();
            if (optCallback.isPresent()) {
                callbackList.add(
                        injector.getInstance(optCallback.get()));
            }
        }

        try {
            for (LittleModule.LifecycleCallback callback : callbackList) {
                log.log(Level.FINE, "Starting up module {0} ...", callback);
                callback.startUp();
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Core bootstrap failed", ex);
            throw new IllegalStateException("Failed to bootstrap core", ex);
        }
        appFactory.setActiveRuntime(injector);
        return injector;
    }

    /**
     * Just calls bootstrap( Injector.class )
     */
    @Override
    public final void bootstrap() {
        bootstrap(Injector.class);
    }

    /**
     * Run shutdown on LifecycleCallbacks and unregister with LittleBootstrap.Factory
     */
    @Override
    public void shutdown() {
        if (!bootstrapDone) {
            throw new IllegalStateException("Cannot shutdown if bootstrap failed");
        }
        for (LittleModule.LifecycleCallback callback : callbackList) {
            try {
                log.log(Level.FINE, "Shutting down module {0} ...", callback);
                callback.startUp();
            } catch (Exception ex) {
                log.log(Level.SEVERE, "Shutdown callback failed for: " + callback, ex);
                throw new IllegalStateException("Failed to bootstrap core", ex);
            }
        }
        final SimpleAppFactory appFactory = SimpleAppFactory.getSingleton();
        appFactory.setActiveRuntime(null);
    }
}
