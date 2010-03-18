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

import com.google.inject.ImplementedBy;
import java.io.IOException;
import java.util.Map;
import littleware.apps.swingbase.model.BaseData;

/**
 * swing.base utilities
 */
@ImplementedBy(SimpleSBTool.class)
public interface SwingBaseTool {
    /**
     * Load the BaseData properties last saved to persistent storage
     *
     * @param appData key to locate stored data - different implementations may
     *                only require app-name or version or whatever
     * @return retrieved properties - appData is unchanged
     * @throws IOException on failure to load data
     */
    public Map<String,String>  loadSavedProps( BaseData appData ) throws IOException;

    /**
     * Shortcut loadSaveProperteis(), then put all the props into appData
     */
    public void loadAndApplySavedProps( BaseData appData ) throws IOException;

    /**
     * Save the base-data props
     */
    public void saveProps( BaseData appData ) throws IOException;
}
