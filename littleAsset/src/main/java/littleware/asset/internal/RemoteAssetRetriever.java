/*
 * Copyright 2011 http://code.google.com/p/littleware/
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.internal;


import java.security.GeneralSecurityException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.asset.AssetType;
import littleware.base.BaseException;
import littleware.base.DataAccessException;
import littleware.base.NoSuchThingException;
import littleware.base.Option;
import littleware.security.AccessDeniedException;




/**
 * Interface for retrieving assets from various sources -
 * different implementations may enforce security and cacheing
 * to different degrees.
 * Intended for internal (between servers and managers) use only -
 * clients should interact with littleware.asset via 
 * the AssetSearchManager implementation - which may manage multiple
 * remote services under the hood.
 * An RemoteAssetRetriever uses the AssetSpecializer associated with a loaded
 * asset's type to fill in the type-specific data for the asset,
 * and enforce type-specific constraints.
 */
public interface RemoteAssetRetriever extends java.rmi.Remote {

    /**
     * Get the asset with the specified id.
     *
     * @param assetId of asset to retrieve
     * @return fully initialized asset.
     * @throws AccessDeniedException if caller does not have permission to read
     *                 the specified asset
     * @throws DataAccessException on database access/interaction failure
     * @throws AssetException some other failure condition
     */
    public Option<Asset> getAsset( UUID sessionId, UUID assetId) throws BaseException,
            GeneralSecurityException, RemoteException;

    /**
     * Get as many of the assets in the given collection of ids as possible.
     *
     * @param idSet set of asset ids to retrieve
     * @return list of assets loaded in order - 2 entries
     *                with the same id may reference the same object,
     *                skips ids that do not exist 
     * @throws NoSuchThingException if requested asset does not exist in the db
     * @throws AccessDeniedException if caller does not have permission to read
     *                 the specified asset
     * @throws DataAccessException on database access/interaction failure
     * @throws AssetException if some other failure condition
     */
    public List<Asset> getAssets( UUID sessionId, Collection<UUID> idSet) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;

    /**
     * Get the Home assets this server has access to
     * for open-ended searches.
     *
     * @return mapping from home name to UUID.
     * @throws DataAccessException on database access/interaction failure
     * @throws AccessDeniedException if caller is not an administrator
     */
    public Map<String, UUID> getHomeAssetIds( UUID sessionId ) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;


    public Map<String, UUID> getAssetIdsFrom( UUID sessionId, UUID fromId,
            AssetType type) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;



    public Map<String, UUID> getAssetIdsFrom( UUID sessionId, UUID fromId
            ) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;

}
