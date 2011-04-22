/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.tracker.internal;

import java.util.UUID;
import littleware.apps.tracker.Product;
import littleware.apps.tracker.ProductAlias;
import littleware.apps.tracker.ProductAlias.PABuilder;
import littleware.asset.Asset;
import littleware.asset.spi.AbstractAsset;
import littleware.asset.spi.AbstractAssetBuilder;

public class SimplePABuilder extends AbstractAssetBuilder<ProductAlias.PABuilder> implements ProductAlias.PABuilder {

    public SimplePABuilder() {
        super(ProductAlias.PA_TYPE);
    }


    @Override
    public PABuilder product(Product value) {
        return (PABuilder) toId(value.getId());
    }

    @Override
    public PABuilder product(ProductAlias value) {
        return (PABuilder) toId(value.getId());
    }

    @Override
    public ProductAlias build() {
        return new SimpleAlias(this);
    }


    public static class SimpleAlias extends AbstractAsset implements ProductAlias {

        private SimpleAlias() {
        }

        private SimpleAlias(SimplePABuilder builder) {
            super(builder);
        }

        @Override
        public UUID getProductId() {
            return getToId();
        }

        @Override
        public PABuilder copy() {
            return (new SimplePABuilder()).copy(this);
        }
    }
}
