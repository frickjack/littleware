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
import littleware.base.validate.ValidationException;

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
        if (assetType.isA(ProductAlias.PA_TYPE)) {
            final ProductAlias pa = asset.narrow();
            if ((null == pa.getProductId()) || pa.getProductId().equals(pa.getId())) {
                throw new ValidationException("Alias cannot reference itself");
            }

            final ProductAlias alias = (ProductAlias) asset;
            final Asset reference = search.getAsset(alias.getProductId()).get();
            if (!(reference.getAssetType().isA(Product.PRODUCT_TYPE)
                    || reference.getAssetType().isA(ProductAlias.PA_TYPE))) {
                throw new ValidationException("ProductAlias must reference a Product or ProductAlias");
            }
        } else if (assetType.isA(Version.VERSION_TYPE)
                || assetType.isA(VersionAlias.VA_TYPE)) {
            final Asset product = search.getAsset(asset.getFromId()).get();
            if (!product.getAssetType().isA(Product.PRODUCT_TYPE)) {
                throw new ValidationException("Version and VersionAlias must link from a Product");
            }
            if (assetType.isA(VersionAlias.VA_TYPE)) {
                final VersionAlias alias = asset.narrow();
                if ((null == alias.getVersionId()) || alias.getVersionId().equals(alias.getId())) {
                    throw new ValidationException("Alias cannot reference itself");
                }

                final Version version = search.getAsset(alias.getVersionId()).get().narrow();
                if (!(version.getAssetType().isA(Version.VERSION_TYPE)
                        || version.getAssetType().isA(VersionAlias.VA_TYPE))) {
                    throw new ValidationException("VersionAlias must reference Version or VersionAlias");
                }
            }
        } else if (assetType.isA(Member.MEMBER_TYPE)
                || assetType.isA(MemberAlias.MA_TYPE)) {
            final Asset version = search.getAsset(asset.getFromId()).get();
            if (!version.getAssetType().isA(Version.VERSION_TYPE)) {
                throw new ValidationException("Member and MemberAlias must link from a Version");
            }
            if (assetType.isA(MemberAlias.MA_TYPE)) {
                final MemberAlias alias = asset.narrow();
                if ((null == alias.getMemberId()) || alias.getMemberId().equals(alias.getId())) {
                    throw new ValidationException("Alias cannot reference itself");
                }
                final Asset member = search.getAsset(alias.getMemberId()).get();
                if (!(member.getAssetType().isA(Member.MEMBER_TYPE)
                        || member.getAssetType().isA(MemberAlias.MA_TYPE))) {
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
