package littleware.asset.server.bootstrap;

import littleware.asset.server.bootstrap.internal.SimpleServerBuilder;
import com.google.inject.Provider;
import java.util.Collection;
import littleware.bootstrap.AppBootstrap;
import littleware.bootstrap.AppModuleFactory;

/**
 * Server-side bootstrap configuration manager
 */
public interface ServerBootstrap extends AppBootstrap {

    public interface ServerBuilder extends AppBootstrap.AppBuilder {
        /**
         * List of littleware modules registered with this bootstrap.
         */
        public Collection<ServerModuleFactory> getServerModuleSet();

        public ServerBuilder addModuleFactory( ServerModuleFactory factory);
        public ServerBuilder removeModuleFactory( ServerModuleFactory factory);        
        @Override
        public ServerBuilder addModuleFactory(AppModuleFactory factory);
        @Override
        public ServerBuilder removeModuleFactory(AppModuleFactory factory);
        @Override
        public ServerBuilder profile(AppProfile value);

        @Override
        public ServerBootstrap build();
    }

    
    public static Provider<ServerBuilder> provider = new Provider<ServerBuilder>() {

        @Override
        public ServerBuilder get() {
            return new SimpleServerBuilder();
        }
    };

}
