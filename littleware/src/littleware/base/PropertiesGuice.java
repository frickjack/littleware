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
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configure the GUICE binding String constants according
 * to the constructor injected Properties.
 * If a property name begins with "datasource", then treat
 * the value as a javax.sql.DataSource URL, configure that
 * DataSource, and bind the @Named Guice constant to the datasource.
 */
public class PropertiesGuice implements Module {

    private static final Logger olog = Logger.getLogger(PropertiesGuice.class.getName());
    private final Properties oprop;

    public PropertiesGuice(Properties prop) {
        oprop = prop;
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
        if (sKey.startsWith("int.")) {
            try {
                int i_value = Integer.parseInt(sValue);
                binder.bindConstant().annotatedWith(Names.named(sKey)).to(i_value);
            } catch (NumberFormatException ex) {
                olog.log(Level.WARNING, "Failed to parse as integer property starting with 'int.': " + sKey +
                        ", " + sValue, ex);
            }
        }
    }

    @Override
    public void configure(Binder binder) {
        for (String sKey : oprop.stringPropertyNames()) {
            final String sValue = oprop.getProperty(sKey);
            bindKeyValue(binder, sKey, sValue);
        }
    }
}
