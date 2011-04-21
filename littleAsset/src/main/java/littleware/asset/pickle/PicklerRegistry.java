/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.pickle;

import com.google.inject.Provider;
import littleware.asset.Asset;
import littleware.asset.AssetType;

/**
 * Interface for registering and retrieving pickler providers
 */
public interface PicklerRegistry<T extends PickleMaker<Asset>> extends Provider<T> {

    public void registerSpecializer(AssetType assetType,
            Provider<? extends T> provideSpecial);
}
