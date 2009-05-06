/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.db.jpa;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import javax.sql.DataSource;
import javax.persistence.EntityManagerFactory;
import org.hibernate.ejb.Ejb3Configuration;

/**
 * Standalone EntityManagerFactory injection with
 * custom Hibernate bootstrap.  Currently just used
 * for isolated unit tests.
 */
public class HibernateGuice extends AbstractGuice {

    @Singleton
    public static class FactoryProvider implements Provider<EntityManagerFactory> {

        private final DataSource odsource;
        private EntityManagerFactory ofactory = null;

        @Inject
        FactoryProvider(@Named("datasource.littleware") DataSource dsource) {
            odsource = dsource;
        }

        @Override
        public EntityManagerFactory get() {
            if (null == ofactory) {
                final Ejb3Configuration config = new org.hibernate.ejb.Ejb3Configuration(). //addAnnotatedClass( classOf[SimpleProqUpload] ).
                        addAnnotatedClass( AssetEntity.class ).
                        addAnnotatedClass( TransactionEntity.class ).
                        setProperty("hibernate.dialect",
                                    "org.hibernate.dialect.MySQLDialect"
                                        );

                config.setDataSource(odsource);
                ofactory = config.buildEntityManagerFactory();
            }
            return ofactory;
        }
    }

    @Override
    public void configure(Binder binder) {
        super.configure( binder );
        binder.bind(EntityManagerFactory.class).toProvider( FactoryProvider.class );
    }
}