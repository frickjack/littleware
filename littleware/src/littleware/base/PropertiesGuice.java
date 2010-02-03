/*
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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Session;

/**
 * Configure the GUICE binding String constants according
 * to the constructor injected Properties.
 * If a mail.smtp.host property is set, then bind javax.mail.Session
 * to a provider that sends mail via SMTP to the host.
 */
public class PropertiesGuice implements Module {

    private static final Logger log = Logger.getLogger(PropertiesGuice.class.getName());
    private final Properties properties;

    public PropertiesGuice(Properties prop) {
        properties = prop;
    }

    /**
     * Get an editable reference to the properties underlying
     * this module.  The properties may be edited/overriden
     * before Guice configuration.
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Shortcut injects this( PropertiesLoader.loadProps( propPath )
     */
    public PropertiesGuice( String propPath ) throws IOException {
        this( PropertiesLoader.get().loadProperties(propPath));
    }

    /** Shortcut to PropertiesLoader.loadProperties() */
    public PropertiesGuice() throws IOException  {
        this( PropertiesLoader.get().loadProperties() );
    }

    /**
     * Guice bind a @Named key constant to the given value.
     * If sValue starts with "int." prefix, then also bind an integer value.
     *
     * @param binder for guice
     * @param sKey
     * @param sValue
     */
    public void bindKeyValue(Binder binder, String sKey, String sValue) {
        binder.bindConstant().annotatedWith(Names.named(sKey)).to(sValue);
        final String clean = sValue.trim();
        if ( clean.startsWith( "http://" ) ) {
            try {
                final URL url = new URL( clean );
                binder.bind( URL.class ).annotatedWith( Names.named( sKey ) ).toInstance(url);
            } catch ( MalformedURLException ex ) {
                log.log( Level.WARNING, "Failed to parse URL-like value: " + clean );
            }
        }
    }

    @Override
    public void configure(Binder binder) {
        for (String sKey : properties.stringPropertyNames()) {
            final String sValue = properties.getProperty(sKey);
            bindKeyValue(binder, sKey, sValue);
        }
        if ( properties.containsKey( "mail.smtp.host" ) ) {
            // then bind mail session
            binder.bind( Session.class ).toInstance(
                    Session.getInstance(properties)
                    );
        }
    }
}
