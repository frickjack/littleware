/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.db;

import com.google.inject.Binder;

import com.google.inject.name.Names;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.naming.NamingException;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import littleware.base.PropertiesGuice;
import oracle.jdbc.pool.OracleDataSource;

/**
 * Specialization extends PropertiesGuice with ability to
 * setup DataSource bindings.
 * Logs and eats exceptions setting up DataSource bindings.
 */
public class DbGuice extends PropertiesGuice {

    private static final Logger log = Logger.getLogger(DbGuice.class.getName());

    public DbGuice(Properties props) {
        super(props);
    }

    public DbGuice(String propPath) throws IOException {
        super(propPath);
    }

    public DbGuice() throws IOException {
        super();
    }

    /**
     * Simple DataSource binder for now
     */
    public void bindDataSource(Binder binder, String s_name,
            String s_url) throws SQLException, NamingException {
        // just hard code to embedded derby provider for now
        log.log(Level.INFO, "Binding DataSource " + s_name + " to " + s_url);
        if (s_url.startsWith("jdbc:derby:")) {
            // figure out if its an embedded or network datasource
            final Pattern pattern = Pattern.compile("jdbc:derby://(\\w+):(\\d+)/(.+)$");
            final Matcher match = pattern.matcher(s_url);
            if (match.find()) {
                /*...
                org.apache.derby.jdbc.ClientDataSource40 dataSource = new org.apache.derby.jdbc.ClientDataSource40 ();
                dataSource.setServerName ( match.group(1) );
                dataSource.setPortNumber( Integer.parseInt( match.group(2) ) );
                dataSource.setDatabaseName ( match.group(3) );
                binder.bind(DataSource.class).annotatedWith(Names.named(s_name)).toInstance(dataSource);
                 * */
                org.logicalcobwebs.proxool.ProxoolDataSource data = new org.logicalcobwebs.proxool.ProxoolDataSource(s_name);
                data.setDriver(org.apache.derby.jdbc.ClientDriver.class.getName());
                data.setDriverUrl(s_url);
                data.setUser("ignore");
                data.setPassword("ignore");
                data.setMinimumConnectionCount(10);
                data.setMaximumConnectionCount(30);
                data.setMaximumActiveTime(60000);
                binder.bind(DataSource.class).annotatedWith(Names.named(s_name)).toInstance(data);

            } else {
                org.apache.derby.jdbc.EmbeddedDataSource40 data = new org.apache.derby.jdbc.EmbeddedDataSource40();
                data.setDatabaseName(s_url.substring("jdbc:derby:".length()));
                binder.bind(DataSource.class).annotatedWith(Names.named(s_name)).toInstance(data);
            }
        } else if (s_url.startsWith("jdbc:oracle:")) {
            final OracleDataSource data = new OracleDataSource();
            data.setURL(s_url);
            binder.bind(DataSource.class).annotatedWith(Names.named(s_name)).toInstance(data);
        } else if (s_url.startsWith("jdbc:postgresql:")) {
            org.logicalcobwebs.proxool.ProxoolDataSource data = new org.logicalcobwebs.proxool.ProxoolDataSource(s_name);
            data.setDriver(org.postgresql.Driver.class.getName());
            data.setDriverUrl(s_url);

            String s_user = "littleware_user";
            String s_password = "secret";

            // set the user/password if possible
            for (String s_param : s_url.split("\\?\\&")) {
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
            binder.bind(DataSource.class).annotatedWith(Names.named(s_name)).toInstance(data);
        } else if (s_url.startsWith("jdbc:mysql:")) {
            com.mysql.jdbc.jdbc2.optional.MysqlDataSource data = new com.mysql.jdbc.jdbc2.optional.MysqlDataSource();
            data.setURL(s_url);
            binder.bind(DataSource.class).annotatedWith(Names.named(s_name)).toInstance(data);
        } else if (s_url.startsWith("jndi:")) {
            DataSource data = (DataSource) new InitialContext().lookup(s_url.substring("jndi:".length()));
            binder.bind(DataSource.class).annotatedWith(Names.named(s_name)).toInstance(data);
        } else {
            log.log(Level.INFO, "Not autobinding datasource of unknown type: " + s_name + " - " + s_url);
        }
    }

    @Override
    public void bindKeyValue(Binder binder, String sKey, String sValue) {
        super.bindKeyValue(binder, sKey, sValue);
        if (sKey.startsWith("datasource")) {
            try {
                bindDataSource(binder, sKey, sValue);
            } catch (Exception ex) {
                throw new IllegalArgumentException("Unable to bind datasource: " + sKey + " - " + sValue,
                        ex);
            }
        }
    }
}
