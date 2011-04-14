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
import littleware.base.LittleRegistry;

/**
 * Registry for AssetSpecializer's.
 */
public interface AssetSpecializerRegistry extends LittleRegistry<AssetType,AssetSpecializer> {

}