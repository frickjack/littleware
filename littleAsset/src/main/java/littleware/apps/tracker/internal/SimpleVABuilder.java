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
import littleware.apps.tracker.Version;
import littleware.apps.tracker.VersionAlias;
import littleware.apps.tracker.VersionAlias.VABuilder;
import littleware.asset.spi.AbstractAsset;
import littleware.asset.Asset;
import littleware.asset.spi.AbstractAssetBuilder;

public class SimpleVABuilder extends AbstractAssetBuilder<VersionAlias.VABuilder> implements VersionAlias.VABuilder {

    public SimpleVABuilder() {
        super(VersionAlias.VA_TYPE);
    }

    @Override
    public VABuilder name(String value) {
        return (VABuilder) super.name( value );
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

    @Override
    public VABuilder from(Asset value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static class SimpleAlias extends AbstractAsset implements VersionAlias {

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
