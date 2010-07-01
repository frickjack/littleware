/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.tracker;

import java.util.UUID;
import littleware.apps.tracker.Version.VersionBuilder;
import littleware.asset.Asset;
import littleware.asset.SimpleAssetBuilder;


public class SimpleVersionBuilder extends SimpleAssetBuilder implements Version.VersionBuilder {

    @Override
    public VersionBuilder copy(Asset value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public VersionBuilder parent(Asset value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public VersionBuilder product(Product value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Version build() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static class SimpleVersion extends SimpleAsset implements Version {

        @Override
        public UUID getProductId() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public VersionBuilder copy() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }

}
