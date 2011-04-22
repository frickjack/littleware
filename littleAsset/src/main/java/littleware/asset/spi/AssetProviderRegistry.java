/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.asset.spi;

import com.google.inject.Provider;
import littleware.asset.AssetBuilder;
import littleware.asset.AssetType;
import littleware.base.LittleRegistry;

/**
 * Registry maps AssetType to object factory
 */
public interface AssetProviderRegistry extends LittleRegistry<AssetType,Provider<? extends AssetBuilder>> {

}
