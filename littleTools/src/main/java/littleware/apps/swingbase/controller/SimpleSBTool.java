/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.swingbase.controller;

import com.google.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import littleware.apps.swingbase.model.BaseData;
import littleware.base.PropertiesLoader;

/**
 * Implementation saves properties to littleware-home/app-name.properties file
 */
public class SimpleSBTool implements SwingBaseTool {

    private static final Logger log = Logger.getLogger(SimpleSBTool.class.getName());
    private final PropertiesLoader propLoader;

    @Inject
    public SimpleSBTool(PropertiesLoader propLoader) {
        this.propLoader = propLoader;
    }

    private File getSaveFile(BaseData appData) {
        return new File(propLoader.getLittleHome().get(), appData.getAppName() + ".properties");

    }

    @Override
    public Map<String, String> loadSavedProps(BaseData appData) throws IOException {
        final File saveFile = getSaveFile(appData);
        final Map<String, String> props = new HashMap<String, String>(appData.getProperties());
        if (saveFile.exists()) {
            final Properties overrides = new Properties();
            final InputStream input = new FileInputStream(saveFile);
            try {
                overrides.load(input);
            } finally {
                input.close();
            }
            for (Map.Entry<Object, Object> entry : overrides.entrySet()) {
                props.put((String) entry.getKey(), (String) entry.getValue());
            }
        }
        return props;
    }

    @Override
    public void loadAndApplySavedProps(BaseData appData) throws IOException {
        for (Map.Entry<String, String> entry : loadSavedProps(appData).entrySet()) {
            appData.putProperty(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void saveProps(BaseData appData) throws IOException {
        final File saveFile = getSaveFile(appData);
        final Properties copy = new Properties();
        copy.putAll(appData.getProperties());
        propLoader.safelySave(copy, saveFile);
    }
}
