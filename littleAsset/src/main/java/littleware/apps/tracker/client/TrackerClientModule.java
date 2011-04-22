/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.tracker.client;

import com.google.inject.Binder;
import com.google.inject.Scopes;
import java.util.logging.Logger;
import littleware.apps.tracker.ProductManager;
import littleware.apps.tracker.internal.SimpleTaskManager;
import littleware.apps.tracker.TaskManager;
import littleware.apps.tracker.TaskQueryManager;
import littleware.asset.client.bootstrap.AbstractClientModule;
import littleware.asset.client.bootstrap.ClientModule;
import littleware.asset.client.bootstrap.ClientModuleFactory;
import littleware.bootstrap.AppBootstrap;
import littleware.bootstrap.AppBootstrap.AppProfile;

/**
 * Force load of TaskQueryManager so SERVICE_HANDLE gets registered,
 * bind TaskQueryManager to TaskQueryManagerService
 */
public class TrackerClientModule extends AbstractClientModule {

    private static final Logger log = Logger.getLogger(TrackerClientModule.class.getName());

    public static class Factory implements ClientModuleFactory {

        @Override
        public ClientModule build(AppProfile profile) {
            return new TrackerClientModule(profile);
        }
    }


    private TrackerClientModule(AppBootstrap.AppProfile profile) {
        super(profile);
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(TaskQueryManager.class).to(TaskQueryManagerService.class);
        binder.bind(TaskManager.class).to(SimpleTaskManager.class).in(Scopes.SINGLETON);
        binder.bind(ProductManager.class).to(SimpleProductManager.class).in(Scopes.SINGLETON);
    }
}
