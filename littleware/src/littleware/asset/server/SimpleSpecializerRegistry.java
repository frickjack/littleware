/*
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.server;

import littleware.asset.AssetType;
import littleware.base.SimpleLittleRegistry;

/**
 * SimpleLittleRegistry based implementation of AssetSpecializerRegistry
 */
public class SimpleSpecializerRegistry 
        extends SimpleLittleRegistry<AssetType<?>,AssetSpecializer>
        implements AssetSpecializerRegistry {
    private final AssetSpecializer ospecial_default = new NullAssetSpecializer ();
    
    /**
     * Override SimpleLittleRegistry to return default NullAssetSpecializer
     * if an AssetSpecializer is not registered for the requested key
     * 
     * @param key AssetType to retrieve the specializer for
     * @return specializer for key or default noop specializer
     */
    @Override
    public AssetSpecializer getService( AssetType<?> key ) {
        AssetSpecializer special = super.getService( key );
        if ( null != special ) {
            return special;
        }
        return ospecial_default;
    }
}
