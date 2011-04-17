/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.asset.internal;

import littleware.asset.spi.AbstractAsset;
import littleware.asset.spi.AbstractAssetBuilder;
import littleware.asset.Asset;
import littleware.asset.AssetBuilder;
import littleware.asset.AssetType;
import littleware.asset.LittleHome;

public class LittleHomeBuilder extends AbstractAssetBuilder<LittleHome.HomeBuilder> implements LittleHome.HomeBuilder {

    private static class HomeAsset extends AbstractAsset implements LittleHome {
        public HomeAsset() {}
        public HomeAsset( LittleHomeBuilder builder ) {
            super( builder );
        }

        @Override
        public LittleHome.HomeBuilder copy() {
            return (new LittleHomeBuilder()).copy( this );
        }
    }

    public LittleHomeBuilder () {
        super( LittleHome.HOME_TYPE );
    }

    @Override
    public LittleHome build() {
        return new HomeAsset( this );
    }
}
