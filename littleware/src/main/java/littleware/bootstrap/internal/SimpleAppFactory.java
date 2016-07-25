package littleware.bootstrap.internal;

import com.google.inject.Injector;
import java.util.Optional;
import littleware.bootstrap.AppBootstrap;
import littleware.bootstrap.LittleBootstrap;


/**
 * Internal implementation of SimpleAppFactory - interacts with AbstractLittleBootstrap
 * to try to maintain singleton littleware runtime
 */
public class SimpleAppFactory implements LittleBootstrap.Factory {

    private Optional<Injector> optActive = Optional.empty();
    
    private SimpleAppFactory(){}

    /**
     * Internal method - set null on shutdown to clear active app
     * @param injector application-scoped injector
     */
    public void setActiveRuntime(Injector value) {
        this.optActive = Optional.ofNullable(value);
    }

    @Override
    public Optional<LittleBootstrap> getActiveRuntime() {
        if (! optActive.isPresent()) {
            return Optional.empty();
        }
        return Optional.of(lookup(LittleBootstrap.class));
    }

    @Override
    public <T> T lookup(Class<T> clazz) {
        if (! optActive.isPresent()) {
            //
            // This bootup process loops back and invokes setActiveRuntime ...
            // Ensures this singleton is properly wired even if the session
            // boots up via another code path ... kind of a crazy handshake
            //
            return AppBootstrap.appProvider.get().build().bootstrap(clazz);
        }
        return optActive.get().getInstance(clazz);
    }


    private static final SimpleAppFactory singleton = new SimpleAppFactory();
    
    public static SimpleAppFactory getSingleton() { return singleton; }
}
