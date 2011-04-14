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
import littleware.apps.tracker.VersionAlias.VABuilder;
import littleware.asset.Asset;
import littleware.asset.SimpleAssetBuilder;

class SimpleVABuilder extends SimpleAssetBuilder implements VersionAlias.VABuilder {

    public SimpleVABuilder() {
        super(VersionAlias.VAType);
    }

    @Override
    public VABuilder name(String value) {
        return (VABuilder) super.name( value );
    }

    @Override
    public VABuilder parent(Asset value) {
        if (!(value instanceof Product)) {
            throw new IllegalArgumentException("VersionAlias parent must be a product");
        }
        return (VABuilder) super.parent(value);
    }

    @Override
    public VABuilder product(Product value) {
        return parent(value);
    }

    @Override
    public VABuilder version(UUID value) {
        return (VABuilder) toId(value);
    }

    @Override
    public VABuilder version(Version value) {
        return version(value.getId());
    }

    @Override
    public VABuilder copy(Asset asset) {
        return (VABuilder) super.copy(asset);
    }

    @Override
    public VersionAlias build() {
        return new SimpleAlias(this);
    }

    public static class SimpleAlias extends SimpleAsset implements VersionAlias {

        private SimpleAlias() {
        }

        private SimpleAlias(SimpleVABuilder builder) {
            super(builder);
        }

        @Override
        public UUID getProductId() {
            return getFromId();
        }

        @Override
        public UUID getVersionId() {
            return getToId();
        }

        @Override
        public VABuilder copy() {
            return (new SimpleVABuilder()).copy(this);
        }
    }
}
