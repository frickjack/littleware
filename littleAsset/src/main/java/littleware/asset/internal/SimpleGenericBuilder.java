/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.asset.internal;

import java.util.UUID;
import littleware.asset.LinkAsset;
import littleware.asset.spi.AbstractAsset;
import littleware.asset.spi.AbstractAssetBuilder;
import littleware.asset.GenericAsset;
import littleware.asset.GenericAsset.GenericBuilder;


public class SimpleGenericBuilder extends AbstractAssetBuilder<GenericAsset.GenericBuilder> implements GenericAsset.GenericBuilder {

    private static class SimpleGeneric extends AbstractAsset implements GenericAsset {

        public SimpleGeneric( ) {}
        public SimpleGeneric( SimpleGenericBuilder builder ) {
            super( builder );
        }
        
        @Override
        public GenericBuilder copy() {
            return (new SimpleGenericBuilder()).copy( this );
        }


        @Override
        public UUID getParentId() {
            return getFromId();
        }
    }

    public SimpleGenericBuilder() {
        super( GenericAsset.GENERIC );
    }

    @Override
    public GenericAsset build() {
        return new SimpleGeneric( this );
    }

}
