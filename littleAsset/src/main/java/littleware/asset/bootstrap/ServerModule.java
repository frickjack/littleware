/*
 * Copyright 2011 http://code.google.com/p/littleware/
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.bootstrap;

import java.util.Collection;
import java.util.Map;
import littleware.asset.AssetType;
import littleware.asset.AssetSpecializer;
import littleware.asset.LittleServerListener;
import littleware.bootstrap.AppModule;

/**
 * Server-side littleware runtime module
 */
public interface ServerModule extends AppModule {

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
