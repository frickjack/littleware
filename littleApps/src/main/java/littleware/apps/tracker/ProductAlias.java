/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.tracker;

import littleware.asset.Asset;
import littleware.asset.AssetBuilder;

/**
 *
 * @author pasquini
 */
public interface ProductAlias extends Asset {
    @Override
    public PABuilder copy();
    
    public interface PABuilder extends AssetBuilder {
        @Override
        public ProductAlias build();
    }
}
