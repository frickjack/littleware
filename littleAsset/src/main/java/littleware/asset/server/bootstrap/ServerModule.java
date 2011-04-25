/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.server.bootstrap;

import java.util.Collection;
import java.util.Map;
import littleware.asset.AssetType;
import littleware.asset.server.AssetSpecializer;
import littleware.asset.server.LittleServerListener;
import littleware.bootstrap.LittleModule;

/**
 * Server-side littleware runtime module
 */
public interface ServerModule extends LittleModule {
    public ServerBootstrap.ServerProfile  getProfile();

    /**
     * Get the collection of asset types this module provides,
     * and the asset-specializer to inject and register with the asset engine.
     */
    public Map<AssetType,Class<? extends AssetSpecializer>> getAssetTypes();

    /**
     * Get collection of LittleServerEvent listeners to register with
     * the server event bus.
     */
    public Collection<Class<? extends LittleServerListener>> getServerListeners();
}
