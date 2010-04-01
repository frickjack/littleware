/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.client;

import com.google.inject.Inject;
import com.google.inject.Injector;
import littleware.security.auth.SessionHelper;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Utility activator takes care of shutting down the
 * bootstraps the session helper, and takes care of client-side
 * injection of just-loaded assets.
 */
public class ClientSessionActivator
        implements BundleActivator, LittleServiceListener {

    private final SessionHelper helper;
    private final Injector injector;

    @Inject
    public ClientSessionActivator(Injector injector, SessionHelper helper) { //, CompositeCacheManager cacheManager) {
        this.helper = helper;
        this.injector = injector;
        ((LittleService) helper).addServiceListener(this);
    }

    @Override
    public void start(BundleContext ctx) throws Exception {
        ((LittleService) helper).start(ctx);
    }

    @Override
    public void stop(BundleContext ctx) throws Exception {
        ((LittleService) helper).stop(ctx);
    }

    @Override
    public void receiveServiceEvent(LittleServiceEvent eventBase) {
        if (eventBase instanceof AssetLoadEvent) {
            final AssetLoadEvent eventLoad = (AssetLoadEvent) eventBase;
            injector.injectMembers(eventLoad.getAsset());
        }
    }
}
