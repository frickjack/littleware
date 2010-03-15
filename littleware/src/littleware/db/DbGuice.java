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
import java.lang.reflect.Proxy;
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
    public void bindDataSource(Binder binder, String name,
            String url) {
        // just hard code to embedded derby provider for now
        log.log(Level.INFO, "Binding DataSource " + name + " to " + url);

        final DataSourceHandler handler = (new GeneralDSHBuilder()).url(url).name(name).build();
        binder.bind(DataSource.class).annotatedWith(Names.named(name)).toInstance(
                (DataSource) Proxy.newProxyInstance(
                                            DataSource.class.getClassLoader(),
                                          new Class[] { DataSource.class },
                                          handler)
                );
        binder.bind(DataSourceHandler.class).annotatedWith(Names.named(name)).toInstance(handler);

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
