package littleware.bootstrap;

import littleware.bootstrap.AppBootstrap.AppProfile;

public interface AppModuleFactory {

    public AppModule build(AppProfile profile);
}
