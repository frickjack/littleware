/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.bootstrap.client;

import java.util.Collection;
import littleware.asset.AssetType;
import littleware.asset.client.LittleServiceListener;
import littleware.bootstrap.client.AppBootstrap.AppProfile;
import littleware.security.auth.ServiceType;

/**
 * Bootstrap module for application-mode bootstrap.
 */
public interface ClientModule extends AppModule {
    /**
     * Get the collection of asset types this module provides
     */
    public Collection<AssetType> getAssetTypes();
    /**
     * Get the collection of service types this module provides
     */
    public Collection<ServiceType> getServiceTypes();
    /**
     * Get the collection of listeners to inject and
     * attach to the SessionHelper.
     * Can also register other listeners with the SessionHelperService
     * via the module's Activator.
     */
    public Collection<Class<? extends LittleServiceListener>> getServiceListeners();
}
