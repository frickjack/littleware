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
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import littleware.base.PropertiesGuice;
import littleware.base.PropertiesLoader;

/**
 * Specialization extends PropertiesGuice with ability to
 * setup DataSource bindings.
 * Logs and eats exceptions setting up DataSource bindings.
 */
public class DbGuice extends PropertiesGuice {

    private static final Logger log = Logger.getLogger(DbGuice.class.getName());

    protected DbGuice(Properties props) {
        super(props);
    }

    /**
     * Factory method
     *
     * @param props to back the new Module with
     */
    public static DbGuice build( Properties props ) {
        return new DbGuice( props );
    }
    /**
     * Shortcut injects this( PropertiesLoader.loadProps( propPath )
     */
    public static DbGuice build(String propPath) throws IOException {
        return build(PropertiesLoader.get().loadProperties(propPath));
    }

    /** Shortcut to PropertiesLoader.loadProperties() */
    public static DbGuice build() throws IOException {
        return build(PropertiesLoader.get().loadProperties());
    }

    /** Shortcut to PropertiesLoader.loadProperties() */
    public static DbGuice build( Class<?> propClass ) throws IOException {
        return build(PropertiesLoader.get().loadProperties( propClass ));
    }

    /**
     * Simple DataSource binder for now
     */
    public void bindDataSource(Binder binder, String name,
            String url) {
        // just hard code to embedded derby provider for now
        log.log(Level.INFO, "Binding DataSource {0} to {1}", new Object[]{name, url});

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
