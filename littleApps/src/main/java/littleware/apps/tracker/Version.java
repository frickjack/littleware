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

import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetBuilder;

/**
 * Product version
 */
public interface Version extends Asset {
    public UUID  getProductId();

    @Override
    public VersionBuilder copy();

    public interface VersionBuilder extends AssetBuilder {
        @Override
        public VersionBuilder copy( Asset value );
        /**
         * Throws IllegalArgumentException if parent is not a product
         */
        @Override
        public VersionBuilder parent( Asset value );
        /**
         * Alias for parent
         */
        public VersionBuilder product( Product value );
        @Override
        public Version build();
    }
}
