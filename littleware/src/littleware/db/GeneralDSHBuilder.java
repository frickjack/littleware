/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.db;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import littleware.base.Maybe;
import littleware.base.Option;
import littleware.base.validate.ValidationException;
import littleware.db.DataSourceHandler.DSHBuilder;
import oracle.jdbc.pool.OracleDataSource;

/**
 * General DataSourceBuilder implementation just resolves
 * jdbc URL to an appropriate db-specific DataSource implementation.
 */
public class GeneralDSHBuilder implements DataSourceHandler.DSHBuilder {

    private String url = null;
    private String name = "";

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public final void setUrl(String value) {
        url(value);
    }

    @Override
    public DataSourceHandler.DSHBuilder url(String value) {
        this.url = value;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public final void setName(String value) {
        name(value);
    }

    @Override
    public DSHBuilder name(String value) {
        name = value;
        return this;
    }

    private static class Handler implements DataSourceHandler {

        private DataSource dsource;
        private String jdbcUrl;

        public Handler(DataSource dsource, String jdbcUrl ) {
            this.dsource = dsource;
            this.jdbcUrl = jdbcUrl;
        }

        @Override
        public DataSource getDataSource() {
            return dsource;
        }

        @Override
        public void setDataSource(DataSource value, String jdbcUrl ) {
            this.dsource = value;
            this.jdbcUrl = jdbcUrl.trim();
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return method.invoke(dsource, args);
        }

        @Override
        public String getJdbcUrl() {
            return jdbcUrl;
        }

        @Override
        public Option<DataSource> resetIfNecessary(String newJdbcUrl, DSHBuilder builder) {
            final String cleanUrl = newJdbcUrl.trim();
            if( cleanUrl.equalsIgnoreCase( getJdbcUrl() ) ) {
                return Maybe.empty();
            }
            final Option<DataSource> result = Maybe.something( dsource );
            setDataSource( builder.url( cleanUrl ).build().getDataSource(), cleanUrl );
            return result;
        }
    }

    @Override
    public DataSourceHandler build() {
        if (null == url) {
            throw new ValidationException("Null URL");
        }

        if (url.startsWith("jdbc:derby:")) {
            // figure out if its an embedded or network datasource
            final Pattern pattern = Pattern.compile("jdbc:derby://(\\w+):(\\d+)/(.+)$");
            final Matcher match = pattern.matcher(url);
            if (match.find()) {
                /*...
                org.apache.derby.jdbc.ClientDataSource40 dataSource = new org.apache.derby.jdbc.ClientDataSource40 ();
                dataSource.setServerName ( match.group(1) );
                dataSource.setPortNumber( Integer.parseInt( match.group(2) ) );
                dataSource.setDatabaseName ( match.group(3) );
                binder.bind(DataSource.class).annotatedWith(Names.named(s_name)).toInstance(dataSource);
                 * */
                org.logicalcobwebs.proxool.ProxoolDataSource data = new org.logicalcobwebs.proxool.ProxoolDataSource(name);
                data.setDriver(org.apache.derby.jdbc.ClientDriver.class.getName());
                data.setDriverUrl(url);
                data.setUser("ignore");
                data.setPassword("ignore");
                data.setMinimumConnectionCount(10);
                data.setMaximumConnectionCount(30);
                data.setSimultaneousBuildThrottle(10);
                data.setMaximumActiveTime(60000);
                return new Handler(data,url);
            } else {
                org.apache.derby.jdbc.EmbeddedDataSource40 data = new org.apache.derby.jdbc.EmbeddedDataSource40();
                data.setDatabaseName(url.substring("jdbc:derby:".length()));
                return new Handler(data, url);
            }
        } else if (url.startsWith("jdbc:oracle:")) {
            try {
                final OracleDataSource data = new OracleDataSource();
                data.setURL(url);
                return new Handler(data, url);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Failed ORACLE setup " + url, ex);
            }
        } else if (url.startsWith("jdbc:postgresql:")) {
            org.logicalcobwebs.proxool.ProxoolDataSource data = new org.logicalcobwebs.proxool.ProxoolDataSource(name);
            data.setDriver(org.postgresql.Driver.class.getName());
            data.setDriverUrl(url);

            String s_user = "littleware_user";
            String s_password = "secret";

            // set the user/password if possible
            for (String s_param : url.split("\\?\\&")) {
                if (s_param.startsWith("user=")) {
                    s_user = s_param.substring("user=".length());
                } else if (s_param.startsWith("password=")) {
                    s_password = s_param.substring("password=".length());
                }
            }
            data.setUser(s_user);
            data.setPassword(s_password);
            data.setMaximumConnectionCount(10);
            data.setMaximumActiveTime(60000);
            return new Handler(data,url);
        } else if (url.startsWith("jdbc:mysql:")) {
            com.mysql.jdbc.jdbc2.optional.MysqlDataSource data = new com.mysql.jdbc.jdbc2.optional.MysqlDataSource();
            data.setURL(url);
            return new Handler(data,url);
        } else if (url.startsWith("jndi:")) {
            try {
                final DataSource data = (DataSource) new InitialContext().lookup(url.substring("jndi:".length()));
                return new Handler(data,url);
            } catch (NamingException ex) {
                throw new IllegalArgumentException("Failed JNDI lookup for " + url, ex);
            }
        } else {
            throw new IllegalArgumentException("Not autobinding datasource of unknown type: " + name + " - " + url);
        }

    }
}
