package littleware.bootstrap;

import littleware.bootstrap.AppBootstrap.AppProfile;

/**
 * Module for application-mode bootstrap.
 */
public interface AppModule extends LittleModule {
    public AppProfile          getProfile();
}
