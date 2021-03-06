package littleware.bootstrap.helper;

import com.google.inject.Binder;
import com.google.inject.Scopes;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.base.UUIDFactory;
import littleware.base.ZipUtil;
import littleware.base.internal.SimpleZipUtil;
import littleware.bootstrap.AppBootstrap;
import littleware.bootstrap.AppBootstrap.AppProfile;
import littleware.bootstrap.AppModule;
import littleware.bootstrap.AppModuleFactory;
import littleware.db.DbGuice;

/**
 * Module sets up PropertiesGuice() bindings from littleware.properties
 */
public class LittlePropsModule extends AbstractAppModule {
    private static final Logger log = Logger.getLogger( LittlePropsModule.class.getName() );

    public static class Factory implements AppModuleFactory {

        @Override
        public AppModule build(AppProfile profile) {
            return new LittlePropsModule( profile );
        }

    }

    private LittlePropsModule( AppBootstrap.AppProfile profile ) {
        super( profile );
    }

    @Override
    public void configure( Binder binder ) {
        try {
            log.log( Level.FINE, "Configuring LittlePropsModule ..." );
            DbGuice.build().configure(binder);
            binder.bind( ZipUtil.class ).to( SimpleZipUtil.class ).in( Scopes.SINGLETON );
            binder.bind( UUID.class ).toProvider( UUIDFactory.class );
            binder.bind( UUIDFactory.class ).in( Scopes.SINGLETON );
            //binder.bind( SessionBootstrap.SessionBuilder.class ).to( SimpleSessionBuilder.class );
        } catch (IOException ex) {
            throw new IllegalStateException( "Unexpected failure loading littleware.properties", ex );
        }
    }
}
