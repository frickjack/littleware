/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server;

import littleware.asset.*;
import java.security.GeneralSecurityException;

import littleware.base.*;

/**
 * Interface for managers of different AssetTypes to implement.
 * The AssetRetriever calls out to an external specializer to decorate
 * the basic Asset pulled out of the database.
 * A specializer may decorate an Asset with support assets
 * and external data as necessary.
 *
 * Implementations go into .server packages, but
 * interface stays in littleware.asset due to our AssetType
 * based specializer mechanism.
 */
public interface AssetSpecializer {

    /**
     * Decorate the input asset with whatever supplemental data
     * is necessary to implement the AssetType supported by this
     * specializer.  Throws the same set of exceptions as getAsset().
     *
     * @param assetIn instance of class returned by a_in.getAssetType ().create ()
     * @return asset, or a new Asset consistent with the data in assetIn
     */
    public <T extends Asset> T narrow( LittleContext ctx, T asset) throws BaseException, AssetException,
            GeneralSecurityException;

    /**
     * Post asset-creation callback made by the AssetManager up to the specializer
     * responsible for the AssetType of the just created asset.
     * Throws the same set of exceptions as AssetManager.createAsset...
     *
     * @param asset just created
     * @param m_asset manager making the callback
     */
    public void postCreateCallback(LittleContext ctx, Asset asset) throws BaseException, AssetException,
            GeneralSecurityException;

    /**
     * Post asset-update callback made by the AssetManager up to the specializer
     * responsible for the AssetType of the just updated asset.
     * Throws the same set of exceptions as AssetManager.postUpdateCallback...
     *
     * @param oldAsset copy of the asset loaded by the AssetManager
     *                    before applying the update
     * @param currentAsset current state of the asset after update
     */
    public void postUpdateCallback(LittleContext ctx, Asset oldAsset, Asset currentAsset) throws BaseException, AssetException,
            GeneralSecurityException;

    /**
     * Post asset-delete callback made by the AssetManager up to the specializer
     * responsible for the AssetType of the just deleted asset.
     * Throws the same set of exceptions as AssetManager.postUpdateCallback...
     *
     * @param asset that just got cleared out
     */
    public void postDeleteCallback(LittleContext ctx, Asset asset) throws BaseException, AssetException,
            GeneralSecurityException;
}
