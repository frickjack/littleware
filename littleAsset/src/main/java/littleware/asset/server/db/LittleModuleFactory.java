/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.db;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;
import littleware.asset.server.bootstrap.ServerModule;
import littleware.asset.server.bootstrap.ServerModuleFactory;
import littleware.asset.server.db.aws.AwsModule;
import littleware.asset.server.db.jpa.HibernateModule;
import littleware.asset.server.db.jpa.J2EEModule;
import littleware.bootstrap.AppBootstrap.AppProfile;

/**
 * Server module factory allocates either db.jpa.HibernateModule,
 * db.jpa.J2EEModule, or db.aws.AwsModule depending on the contents
 * of LittleModuleFactory.properties ...
 */
public class LittleModuleFactory implements ServerModuleFactory {
    private static final Logger log = Logger.getLogger( LittleModuleFactory.class.getName() );
    
    @Override
    public ServerModule buildServerModule(AppProfile profile) {
        final Properties props;
        try {
            props = littleware.base.PropertiesLoader.get().loadProperties( getClass() );
        } catch (IOException ex) {
            throw new IllegalStateException( "Failed to load database configuration", ex );
        }
        final String provider = props.getProperty( "databaseProvider", "hibernate" ).toLowerCase();
        if ( provider.equals( "aws" ) ) {
            return new AwsModule( profile );
        } else if ( provider.equals( "j2ee" ) ) {
            return new J2EEModule( profile );
        } else {
            return new HibernateModule( profile );
        }
    }
    
}
