/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.swingbase.model;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Singleton;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import littleware.base.ValidationException;


/**
 * Simple direct implementation of BaseData
 */
public class SimpleBaseData implements BaseData {

    private static final Logger log = Logger.getLogger(SimpleBaseData.class.getName());
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final String appName;
    private final String version;
    private final URL helpUrl;
    private Map<String, String> properties = Collections.emptyMap();

    protected SimpleBaseData(String appName, String version, URL helpUrl,
            Map<String, String> props) {
        this.appName = appName;
        this.version = version;
        this.helpUrl = helpUrl;
        this.properties = props;
    }

    @Override
    public String getAppName() {
        return appName;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public URL getHelpUrl() {
        return helpUrl;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public void putProperty(String key, String value) {
        final Map<String, String> map = new HashMap<String, String>();
        map.putAll(properties);
        if (!map.containsKey(key)) {
            throw new IllegalArgumentException("Invalid key: " + key);
        }
        final String old = map.get(key);
        map.put(key, value);
        properties = ImmutableMap.copyOf(map);
        support.firePropertyChange(key, old, value);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    protected PropertyChangeSupport getSupport() {
        return support;
    }

    
    public static class SBDBuilder implements BaseData.BDBuilder {
        private String appName;
        private String version;
        private URL helpUrl;
        private final Map<String,String>  properties = new HashMap<String,String>();

        @Override
        public BDBuilder appName(String value) {
            this.appName = value;
            return this;
        }

        @Override
        public BDBuilder version(String value) {
            this.version = value;
            return this;
        }

        @Override
        public BDBuilder helpUrl(URL value) {
            this.helpUrl = value;
            return this;
        }

        @Override
        public BDBuilder putAllProps(Map<? extends Object, ? extends Object> value) {
            for( Map.Entry<? extends Object,? extends Object> entry : value.entrySet() ) {
                properties.put( entry.getKey().toString(), 
                        entry.getValue().toString()
                        );
            }
            return this;
        }

        @Override
        public BDBuilder putProp(String key, String value) {
            properties.put( key, value );
            return this;
        }

        @Override
        public BaseData build() {
            if (!appName.matches("^\\w+$")) {
                throw new ValidationException("Illegal appname: " + appName);
            }
            return new SimpleBaseData( appName, version, helpUrl, properties );
        }
    }
}
