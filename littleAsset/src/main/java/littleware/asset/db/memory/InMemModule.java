package littleware.asset.db.memory;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import littleware.asset.LittleTransaction;
import littleware.asset.bootstrap.AbstractServerModule;
import littleware.asset.db.DbCommandManager;
import littleware.asset.db.DbInitializer;
import littleware.bootstrap.AppBootstrap;

/**
 * Base bootstrap module for in memory database backend 
 */
public abstract class InMemModule extends AbstractServerModule {
    private static final Logger log = Logger.getLogger( InMemModule.class.getName() );
    
    

    /**
     * Handler for initializing an in-mem environment for littleware use.
     */
    public static class SetupHandler implements Runnable, Provider<DbCommandManager> {

        private boolean isDomainInitialized = false;
        private final DbCommandManager mgr;
        private final DbInitializer dbInit;

        @Inject
        public SetupHandler( InMemDbManager mgr,
                DbInitializer dbInit
                ) {
            this.mgr = mgr;
            this.dbInit = dbInit;
        }

        @Override
        public void run() {
            dbInit.initDB( mgr );
        }

        @Override
        public DbCommandManager get() {
            if (!isDomainInitialized) {
                this.run();
                isDomainInitialized = true;
            }
            return mgr;
        }
    }
    

    public InMemModule( AppBootstrap.AppProfile profile ) {
        super( profile );
    }
    
    
    @Override
    public void configure( Binder binder ) {
        log.log( Level.FINE, "Configuring JPA database access" );
        binder.bind( LittleTransaction.class ).to( InMemTransaction.class );
        binder.bind(DbCommandManager.class).to(InMemDbManager.class).in(Scopes.SINGLETON);
    }
}
