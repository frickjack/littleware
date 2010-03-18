/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.swingclient.controller;

import java.io.IOException;
import java.util.Map;
import littleware.apps.swingbase.model.BaseData;

/**
 * Implementation saves properties to littleware-home/app-name.properties file
 */
public class SimpleSBTool implements SwingBaseTool {

    @Override
    public Map<String, String> loadSavedProps(BaseData appData) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void loadAndApplySavedProps(BaseData appData) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void saveProps(BaseData appData) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
