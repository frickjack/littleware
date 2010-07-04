/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.tracker.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.logging.Logger;
import littleware.apps.tracker.Member;
import littleware.apps.tracker.MemberAlias;
import littleware.apps.tracker.Product;
import littleware.apps.tracker.ProductAlias;
import littleware.apps.tracker.Version;
import littleware.apps.tracker.VersionAlias;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.asset.AssetSearchManager;
import littleware.asset.AssetType;
import littleware.asset.server.NullAssetSpecializer;
import littleware.base.BaseException;
import littleware.base.ValidationException;

/**
 * Specializer for product-tracking asset types
 */
@Singleton
public class SimpleProductSpecializer extends NullAssetSpecializer {

    private static final Logger log = Logger.getLogger(SimpleProductSpecializer.class.getName());
    private final AssetSearchManager search;

    @Inject
    public SimpleProductSpecializer(AssetSearchManager search) {
        this.search = search;
    }

    @Override
    public <T extends Asset> T narrow(T asset) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return asset;
    }

    /**
     * Run Product related assets through validation check
     */
    @Override
    public void postCreateCallback(Asset asset) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        final AssetType assetType = asset.getAssetType();
        if (assetType.isA(ProductAlias.PAType)) {
            if ((null == asset.getToId()) || asset.getToId().equals(asset.getId())) {
                throw new ValidationException("Alias cannot reference itself");
            }

            final ProductAlias alias = (ProductAlias) asset;
            final Asset reference = search.getAsset(alias.getProductId()).get();
            if (!(reference.getAssetType().isA(Product.ProductType)
                    || reference.getAssetType().isA(ProductAlias.PAType))) {
                throw new ValidationException("ProductAlias must reference a Product or ProductAlias");
            }
        } else if (assetType.isA(Version.VersionType)
                || assetType.isA(VersionAlias.VAType)) {
            final Asset product = search.getAsset(asset.getFromId()).get();
            if (!product.getAssetType().isA(Product.ProductType)) {
                throw new ValidationException("Version and VersionAlias must link from a Product");
            }
            if (assetType.isA(VersionAlias.VAType)) {
                if ((null == asset.getToId()) || asset.getToId().equals(asset.getId())) {
                    throw new ValidationException("Alias cannot reference itself");
                }

                final Asset version = search.getAsset(asset.getToId()).get();
                if (!(version.getAssetType().isA(Version.VersionType)
                        || version.getAssetType().isA(VersionAlias.VAType))) {
                    throw new ValidationException("VersionAlias must reference Version or VersionAlias");
                }
            }
        } else if (assetType.isA(Member.MemberType)
                || assetType.isA(MemberAlias.MAType)) {
            final Asset version = search.getAsset(asset.getFromId()).get();
            if (!version.getAssetType().isA(Version.VersionType)) {
                throw new ValidationException("Member and MemberAlias must link from a Version");
            }
            if (assetType.isA(MemberAlias.MAType)) {
                if ((null == asset.getToId()) || asset.getToId().equals(asset.getId())) {
                    throw new ValidationException("Alias cannot reference itself");
                }
                final Asset member = search.getAsset(asset.getToId()).get();
                if (!(member.getAssetType().isA(Member.MemberType)
                        || member.getAssetType().isA(MemberAlias.MAType))) {
                    throw new ValidationException("MemberAlias must reference a Member or MemberAlias");
                }
            }
        }
    }

    @Override
    public void postUpdateCallback(Asset oldAsset, Asset asset) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        postCreateCallback(asset);
    }

    @Override
    public void postDeleteCallback(Asset asset) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
    }
}
