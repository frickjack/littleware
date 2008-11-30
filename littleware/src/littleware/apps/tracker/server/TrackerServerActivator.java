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

package littleware.apps.tracker.server;

import com.google.inject.Inject;
import littleware.apps.tracker.TrackerAssetType;
import littleware.asset.server.AssetSpecializerRegistry;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Just register the tracker bundle Guice bindings.
 */
public class TrackerServerActivator implements BundleActivator {

    /**
     * Inject the specializer registry and the tracker specializer
     *
     * @param registry_special
     * @param special_tracker
     */
    @Inject
    public TrackerServerActivator( AssetSpecializerRegistry registry_special,
            SimpleTrackerManager special_tracker
            ) {
        registry_special.registerService( TrackerAssetType.COMMENT, special_tracker);
        registry_special.registerService( TrackerAssetType.DEPENDENCY, special_tracker );
        registry_special.registerService( TrackerAssetType.TASK, special_tracker );
        registry_special.registerService( TrackerAssetType.QUEUE, special_tracker );
    }

    /** NOOP for now */
    public void start(BundleContext ctx) throws Exception {
    }

    /** NOOP for now */
    public void stop(BundleContext ctx) throws Exception {
    }

}
