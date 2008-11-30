/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.base;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.name.Names;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.derby.jdbc.EmbeddedDataSource40;
import org.logicalcobwebs.proxool.ProxoolDataSource;


/**
 * Configure the GUICE binding String constants according
 * to the constructor injected Properties.
 * If a property name begins with "datasource", then treat
 * the value as a javax.sql.DataSource URL, configure that
 * DataSource, and bind the @Named Guice constant to the datasource.
 */
public class PropertiesGuice implements Module {
    private static final Logger olog = Logger.getLogger( PropertiesGuice.class.getName() );

    private final Properties oprop;

    public PropertiesGuice( Properties prop ) {
        oprop = prop;
    }

    public void configure( Binder binder_in ) {
        for( String s_key : oprop.stringPropertyNames() ) {
            String s_value = oprop.getProperty(s_key);
            binder_in.bindConstant().annotatedWith( Names.named(s_key)).to( s_value );
            if ( s_key.startsWith( "datasource" ) ) {
                try {
                    bindDataSource( binder_in, s_key, s_value );
                } catch ( Exception ex ) {
                    throw new IllegalArgumentException( "Unable to bind datasource: " + s_key + " - " + s_value,
                            ex );
                }
            } else if ( s_key.startsWith( "int." ) ) {
                try {
                    int i_value = Integer.parseInt( s_value );
                    binder_in.bindConstant().annotatedWith( Names.named( s_key ) ).to( i_value );
                } catch ( NumberFormatException ex ) {
                    olog.log( Level.WARNING, "Failed to parse as integer property starting with 'int.': " + s_key +
                            ", " + s_value, ex
                            );
                }
            }
        }
    }


    /**
     * Verify simple DataSource binder for now
     */
    private void bindDataSource(Binder binder, String s_name,
            String s_url) throws SQLException, NamingException {
        // just hard code to embedded derby provider for now
        if (s_url.startsWith("jdbc:derby:")) {
            EmbeddedDataSource40 data = new org.apache.derby.jdbc.EmbeddedDataSource40();
            data.setDatabaseName(s_url.substring("jdbc:derby:".length()));
            olog.log(Level.FINE, "Binding DataSource with name " + s_name);
            binder.bind(DataSource.class).annotatedWith(Names.named(s_name)).toInstance(data);
            /*...
             No need to support Oracle yet ...
        } else if (s_url.startsWith("jdbc:oracle:")) {
            OracleDataSource data = new OracleDataSource();
            data.setURL(s_url);
            binder.bind(DataSource.class).annotatedWith(Names.named(s_name)).toInstance(data);
              ..*/
        } else if (s_url.startsWith("jdbc:postgresql:")) {
            ProxoolDataSource data = new ProxoolDataSource( s_url );
            data.setDriverUrl( s_url );
            binder.bind(DataSource.class).annotatedWith(Names.named(s_name)).toInstance(data);
        } else if (s_url.startsWith("jndi:")) {
            DataSource data = (DataSource) new InitialContext().lookup(s_url.substring("jndi:".length()));
            binder.bind(DataSource.class).annotatedWith(Names.named(s_name)).toInstance(data);
        } else {
            olog.log(Level.INFO, "Not autobinding datasource of unknown type: " + s_name + " - " + s_url);
        }
    }


}
