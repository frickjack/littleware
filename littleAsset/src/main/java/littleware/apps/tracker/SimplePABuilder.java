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
import littleware.apps.tracker.ProductAlias.PABuilder;
import littleware.asset.Asset;
import littleware.asset.SimpleAssetBuilder;

public class SimplePABuilder extends SimpleAssetBuilder implements ProductAlias.PABuilder {

    public SimplePABuilder() {
        super(ProductAlias.PAType);
    }

    @Override
    public PABuilder name(String value) {
        return (PABuilder) super.name(value);
    }

    @Override
    public PABuilder parent(Asset value) {
        return (PABuilder) super.parent(value);
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

    @Override
    public PABuilder copy(Asset source) {
        return (PABuilder) super.copy(source);
    }

    public static class SimpleAlias extends SimpleAsset implements ProductAlias {

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
