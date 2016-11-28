package littleware.asset.server.bootstrap;

import littleware.bootstrap.AppBootstrap;

public interface ServerModuleFactory {

    /**
     * Build the server module configured for the given profile
     */
    public ServerModule buildServerModule( AppBootstrap.AppProfile profile );
}
