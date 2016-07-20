package littleware.bootstrap.helper;

import java.util.Optional;
import littleware.bootstrap.AppBootstrap;
import littleware.bootstrap.AppBootstrap.AppProfile;
import littleware.bootstrap.AppModule;

public abstract class AbstractAppModule implements AppModule {
    private final AppProfile profile;


    public AbstractAppModule( AppBootstrap.AppProfile profile
            ) {
        this.profile = profile;
    }

    @Override
    public AppProfile getProfile() {
        return profile;
    }

    @Override
    public Optional<? extends Class<? extends LifecycleCallback>> getCallback() {
        return Optional.empty();
    }

}
