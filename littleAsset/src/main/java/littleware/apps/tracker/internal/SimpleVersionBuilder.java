/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.tracker.internal;

import com.google.inject.Inject;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.UUID;
import littleware.apps.tracker.Member;
import littleware.apps.tracker.Product;
import littleware.apps.tracker.Version;
import littleware.apps.tracker.Version.VersionBuilder;
import littleware.asset.spi.AbstractAsset;
import littleware.asset.Asset;
import littleware.asset.AssetSearchManager;
import littleware.asset.spi.AbstractAssetBuilder;
import littleware.base.BaseException;

public class SimpleVersionBuilder extends AbstractAssetBuilder<Version.VersionBuilder> implements Version.VersionBuilder {

    public SimpleVersionBuilder() {
        super(Version.VersionType);
    }

    @Override
    public VersionBuilder name(String value) {
        return (VersionBuilder) super.name(value);
    }

    @Override
    public VersionBuilder copy(Asset value) {
        return (VersionBuilder) super.copy(value);
    }


    @Override
    public VersionBuilder product(Product value) {
        return parent(value);
    }

    @Override
    public Version build() {
        return new SimpleVersion(this);
    }

    public static class SimpleVersion extends AbstractAsset implements Version {

        private transient AssetSearchManager search;

        private SimpleVersion() {
        }

        private SimpleVersion(SimpleVersionBuilder builder) {
            super(builder);
        }

        @Inject
        public void injectMe(AssetSearchManager search) {
            this.search = search;
        }

        @Override
        public UUID getProductId() {
            return getFromId();
        }

        @Override
        public VersionBuilder copy() {
            return (new SimpleVersionBuilder()).copy(this);
        }

        @Override
        public Map<String, UUID> getMembers() throws BaseException, GeneralSecurityException, RemoteException {
            return search.getAssetIdsFrom(getId(), Member.MemberType);
        }
    }
}
