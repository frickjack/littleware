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
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.validation.ValidationException;
import littleware.base.PropertiesLoader;

/**
 * Simple direct implementation of BaseData
 */
@Singleton
public class SimpleBaseData implements BaseData {
    private static final Logger log = Logger.getLogger( SimpleBaseData.class.getName() );

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final String appName;
    private final String version;
    private final URL helpUrl;
    private Map<String, String> properties = Collections.emptyMap();

    /**
     * Constructor injects properties, and attempts to load
     * saved properties overrides.
     *
     * @param appName
     * @param version
     * @param helpUrl
     * @param defaultProperties establishes app defaults
     * @param propertiesLoader loads saved defaults overlayed over defaultProperties
     *        to establish the getProperties value
     */
    @Inject
    public SimpleBaseData(@Named("BaseData.appName") String appName,
            @Named("BaseData.version") String version,
            @Named("BaseData.helpUrl") URL helpUrl,
            @Named("BaseData.defaultProperties") Properties defaultProperties,
            PropertiesLoader propertiesLoader) {
        if (!appName.matches("^\\w+$")) {
            throw new ValidationException("Illegal appname: " + appName);
        }
        this.appName = appName;
        this.version = version;
        this.helpUrl = helpUrl;

        Properties props = defaultProperties;
        try {
             props = propertiesLoader.applyOverrides(defaultProperties,
                    propertiesLoader.loadProperties(appName));

        } catch (Exception ex) {
            log.log( Level.WARNING, "Failed to load properties for " + appName, ex );
        }
        final ImmutableMap.Builder<String,String> propBuilder = ImmutableMap.builder();
        for( Map.Entry<Object,Object> entry : props.entrySet() ) {
           propBuilder.put( (String) entry.getKey(), (String) entry.getValue() );
        }
        this.properties = propBuilder.build();
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
    public URL getHelpURL() {
        return helpUrl;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public void putProperty(String key, String value) {
        final Map<String,String> map = new HashMap<String,String>();
        map.putAll(properties);
        if ( ! map.containsKey(key)) {
            throw new IllegalArgumentException( "Invalid key: " + key );
        }
        final String old = map.get( key );
        map.put( key, value );
        properties = ImmutableMap.copyOf( map );
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

    protected PropertyChangeSupport getSupport() { return support; }
}
