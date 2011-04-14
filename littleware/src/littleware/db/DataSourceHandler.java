/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.db;

import com.google.inject.ImplementedBy;
import java.lang.reflect.InvocationHandler;
import javax.sql.DataSource;
import littleware.base.Maybe;

/**
 * Dynamic-proxy handler for SQL DataSource allows
 * an app to change a DataSource URL at runtime.
 * Handy for simple tools where the user can specify
 * a database URL as an application parameter
 */
public interface DataSourceHandler extends InvocationHandler {

    public DataSource getDataSource();
    public String     getJdbcUrl();

    public void setDataSource(DataSource value, String jdbcUrl );

    /**
     * Little utility builds and assigns a new data source
     * if newJdbcUrl does not match getJdbcUrl ignore-casee
     *
     * @param newJdbcUrl
     * @param builder
     * @return old DataSource if reset, otherwise empty
     */
    public Maybe<DataSource> resetIfNecessary( String newJdbcUrl, DSHBuilder builder );

    @ImplementedBy(GeneralDSHBuilder.class)
    public interface DSHBuilder {
        /**
         * JDBC URL for DataSource to construct and assign to a handler
         */
        public String getUrl();
        public void setUrl(String value);
        public DSHBuilder url(String value);


        /**
         * Name to assign to data source
         */
        public String getName();
        public void setName( String value );
        public DSHBuilder name( String value );

        public DataSourceHandler build();
    }

}
