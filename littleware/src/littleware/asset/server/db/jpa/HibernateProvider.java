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

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.hibernate.ejb.Ejb3Configuration;

/**
 * Subtypes can specialize and rebind this frickjack to add
 * additional entity types
 * for testing.
 */
@Singleton
public class HibernateProvider implements Provider<EntityManagerFactory> {

    private final DataSource odsource;
    private EntityManagerFactory ofactory = null;
    private final List<Class<?>> ovExtraEntity = new ArrayList<Class<?>>();

    @Inject
    public HibernateProvider(@Named("datasource.littleware") DataSource dsource) {
        odsource = dsource;
    }

    /**
     * Allow subtypes to extend the set of registered entity classes
     */
    protected HibernateProvider(DataSource dsource,
            List<Class<?>> vExtraEntity) {
        odsource = dsource;
        ovExtraEntity.addAll(vExtraEntity);
    }

    @Override
    public EntityManagerFactory get() {
        if (null == ofactory) {
            final Ejb3Configuration config = new org.hibernate.ejb.Ejb3Configuration(). //addAnnotatedClass( classOf[SimpleProqUpload] ).
                    addAnnotatedClass(AssetEntity.class).
                    addAnnotatedClass(TransactionEntity.class).
                    addAnnotatedClass(AssetTypeEntity.class).
                    //setProperty("hibernate.show_sql", "true").
                    setProperty("hibernate.dialect",
                    "org.hibernate.dialect.MySQLDialect");
            for (Class<?> entityClass : ovExtraEntity) {
                config.addAnnotatedClass(entityClass);
            }

            config.setDataSource(odsource);
            ofactory = config.buildEntityManagerFactory();
        }
        return ofactory;
    }
}