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

import com.google.inject.Inject;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.UUID;
import littleware.apps.tracker.Product.ProductBuilder;
import littleware.asset.Asset;
import littleware.asset.AssetSearchManager;
import littleware.asset.SimpleAssetBuilder;
import littleware.base.BaseException;

public class SimpleProductBuilder extends SimpleAssetBuilder implements Product.ProductBuilder {

    public SimpleProductBuilder() {
        super(Product.ProductType);
    }

    @Override
    public ProductBuilder name(String value) {
        return (ProductBuilder) super.name(value);
    }

    @Override
    public ProductBuilder copy(Asset value) {
        return (ProductBuilder) super.copy(value);
    }

    @Override
    public ProductBuilder parent(Asset value) {
        return (ProductBuilder) super.parent(value);
    }

    @Override
    public Product build() {
        return new SimpleProduct(this);
    }

    public static class SimpleProduct extends SimpleAsset implements Product {

        private AssetSearchManager search;

        public SimpleProduct() {
        }

        public SimpleProduct(SimpleProductBuilder builder) {
            super(builder);
        }

        @Inject
        public void injectMe(AssetSearchManager search) {
            this.search = search;
        }

        @Override
        public UUID getTaskQueue() throws BaseException, GeneralSecurityException, RemoteException {
            return search.getAssetIdsFrom(getId(), TrackerAssetType.QUEUE).get("TaskQueue");
        }

        @Override
        public Map<String, UUID> getDepends() throws BaseException, GeneralSecurityException, RemoteException {
            return search.getAssetIdsFrom( getId(), ProductAlias.PAType );
        }

        @Override
        public Map<String, UUID> getSubProducts() throws BaseException, GeneralSecurityException, RemoteException {
            return search.getAssetIdsFrom(getId(), getAssetType());
        }

        @Override
        public Map<String, UUID> getVersions() throws BaseException, GeneralSecurityException, RemoteException {
            return search.getAssetIdsFrom(getId(), Version.VersionType );
        }

        @Override
        public Map<String, UUID> getVersionAliases() throws BaseException, GeneralSecurityException, RemoteException {
            return search.getAssetIdsFrom(getId(), VersionAlias.VAType );
        }

        @Override
        public ProductBuilder copy() {
            return new SimpleProductBuilder().copy(this);
        }
    }
}
