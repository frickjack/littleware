package littleware.asset.db;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.bootstrap.ServerModule;
import littleware.asset.db.jpa.HibernateModule;
import littleware.asset.db.jpa.J2EEModule;
import littleware.bootstrap.AppModuleFactory;
import littleware.bootstrap.AppBootstrap.AppProfile;

/**
 * Server module factory allocates either db.jpa.HibernateModule,
 * db.jpa.J2EEModule, or db.aws.AwsModule depending on the contents
 * of LittleModuleFactory.properties ...
 */
public class LittleModuleFactory implements AppModuleFactory {
    private static final Logger log = Logger.getLogger( LittleModuleFactory.class.getName() );
    
    public enum Driver { Hibernate, J2EE, AWS };
    
    /**
     * Applications can override the default database driver before server bootstrap
     */
    public static Driver  dbDriver = Driver.Hibernate;
    
    static {
        final Properties props;
        try {
            props = littleware.base.PropertiesLoader.get().loadProperties( LittleModuleFactory.class );
        } catch (IOException ex) {
            throw new IllegalStateException( "Failed to load database configuration", ex );
        }
        
        final String provider = props.getProperty( "databaseProvider", "hibernate" ).toLowerCase();
        if ( provider.equals( "aws" ) ) {
            dbDriver = Driver.AWS;
        } else if ( provider.equals( "j2ee" ) ) {
            dbDriver = Driver.J2EE;
        } else {
            dbDriver = Driver.Hibernate;
        }        
    }
    
    @Override
    public ServerModule build(AppProfile profile) {
        if ( dbDriver.equals( Driver.AWS ) ) {
            log.log( Level.INFO, "Configuring AWS backend" );
            throw new UnsupportedOperationException( "AWS SimpleDB backend deprecated" );
        } else if ( dbDriver.equals( Driver.J2EE ) ) {
            log.log( Level.INFO, "Configuring J2EE JPA backend" );
            return new J2EEModule( profile );
        } else {
            log.log( Level.INFO, "Configuring Hibernate backend" );
            return new HibernateModule( profile );
        }
    }
    
}
