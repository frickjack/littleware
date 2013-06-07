/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.db.jpa;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;


@Singleton
public class HibernateProvider implements Provider<EntityManagerFactory> {

    private final DataSource dataSource;
    private final String dataSourceURL;
    private EntityManagerFactory emFactory = null;


    @Inject
    public HibernateProvider(@Named("datasource.littleware") DataSource dsource,
            @Named("datasource.littleware") String sDatasourceUrl) {
        dataSource = dsource;
        dataSourceURL = sDatasourceUrl;
    }


    @Override
    public EntityManagerFactory get() {
        if (null == emFactory) {
            LittleDriver.setDataSource( dataSource );
            LittleContext.setDataSource( dataSource );
            if ( null == System.getProperty(  "java.naming.factory.initial" ) ) {
                System.setProperty( "java.naming.factory.initial", LittleContext.Factory.class.getName() );
            }
            
            emFactory = Persistence.createEntityManagerFactory( "littlewarePU" );
        }
        return emFactory;
    }
}
