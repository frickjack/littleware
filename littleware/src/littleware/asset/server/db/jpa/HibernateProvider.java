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
    private final String osUrl;
    private EntityManagerFactory ofactory = null;
    private final List<Class<?>> ovExtraEntity = new ArrayList<Class<?>>();

    @Inject
    public HibernateProvider(@Named("datasource.littleware") DataSource dsource,
            @Named("datasource.littleware") String sDatasourceUrl) {
        odsource = dsource;
        osUrl = sDatasourceUrl;
    }

    /**
     * Allow subtypes to extend the set of registered entity classes
     */
    protected HibernateProvider(DataSource dsource,
            String         sDatasourceUrl,
            List<Class<?>> vExtraEntity) {
        odsource = dsource;
        osUrl = sDatasourceUrl;
        ovExtraEntity.addAll(vExtraEntity);
    }

    @Override
    public EntityManagerFactory get() {
        if (null == ofactory) {
            final Ejb3Configuration config = new org.hibernate.ejb.Ejb3Configuration(). //addAnnotatedClass( classOf[SimpleProqUpload] ).
                    addAnnotatedClass(AssetEntity.class).
                    addAnnotatedClass(TransactionEntity.class).
                    addAnnotatedClass(AssetTypeEntity.class);
            config.setProperty("hibernate.show_sql", "true"); //.jdbc:derby://localhost:1527/littleware
            if (osUrl.toLowerCase().indexOf("javadb") > -1) {
                config.setProperty("hibernate.dialect",
                        "org.hibernate.dialect.DerbyDialect");
            } else {
                config.setProperty("hibernate.dialect",
                        "org.hibernate.dialect.MySQLDialect");
            }
            for (Class<?> entityClass : ovExtraEntity) {
                config.addAnnotatedClass(entityClass);
            }

            config.setDataSource(odsource);
            ofactory = config.buildEntityManagerFactory();
        }
        return ofactory;
    }
}
