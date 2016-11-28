package littleware.asset.server.db.jpa;

import com.google.inject.Binder;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManagerFactory;
import littleware.bootstrap.AppBootstrap;
import littleware.db.DbGuice;

/**
 * Standalone EntityManagerFactory injection with
 * custom Hibernate bootstrap.  Currently just used
 * for isolated unit tests.
 */
public class HibernateModule extends AbstractGuice {

    private static final Logger log = Logger.getLogger(HibernateModule.class.getName());

    public HibernateModule(AppBootstrap.AppProfile profile) {
        super(profile);
    }

    @Override
    public void configure(Binder binder) {
        super.configure(binder);
        log.log(Level.FINE, "Configuring JPA in standalone (hibernate) mode ...");
        try {
            DbGuice.build("littleware_jdbc.properties").configure(binder);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load littleware_jdbc.properties", ex);
        }
        binder.bind(EntityManagerFactory.class).toProvider(HibernateProvider.class);
    }
}