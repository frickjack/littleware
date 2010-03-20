/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.swingbase;

import com.google.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.apps.swingbase.controller.SwingBaseTool;
import littleware.apps.swingbase.model.BaseData;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Performs swingbase setup and teardown.
 * At start-time - just load the BaseData properties
 * from storage.
 */
public class SwingBaseActivator implements BundleActivator {
    private static final Logger log = Logger.getLogger( SwingBaseActivator.class.getName() );
    private final BaseData data;
    private final SwingBaseTool tool;

    @Inject
    public SwingBaseActivator( BaseData data, SwingBaseTool tool ) {
        this.data = data;
        this.tool = tool;
    }

    @Override
    public void start(BundleContext bc) {
        try {
            tool.loadAndApplySavedProps(data);
        } catch ( Exception ex ) {
            log.log( Level.WARNING, "Failed to load saved session properties", ex );
        }
    }

    @Override
    public void stop(BundleContext bc) throws Exception {
    }

}
