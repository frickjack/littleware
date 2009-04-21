/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.client;

import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.asset.AssetPath;
import littleware.asset.AssetSearchManager;
import littleware.asset.AssetType;
import littleware.base.BaseException;
import littleware.base.Cache;
import littleware.security.auth.client.ClientCache;

/**
 * Smart proxy for AssetSearchManager
 */
public class SimpleAssetSearchService extends SimpleLittleService implements AssetSearchService {
    private static final long serialVersionUID = 1830540427533232520L;

    // Do not use final with Serializable stuff
    private AssetSearchManager  oserver;

    /** NOOP constructor just to support serialization */
    protected SimpleAssetSearchService() {

    }

    /**
     * Inject the server that does not implement LittleService event support
     *
     * @param server to wrap with events
     */
    public SimpleAssetSearchService( AssetSearchManager server ) {
        oserver = server;
        if ( oserver instanceof LittleService ) {
            throw new IllegalArgumentException( "Attempt to double wrap LittleService smart proxy" );
        }
    }

    @Override
    public <T extends Asset> T getByName(String s_name, AssetType<T> n_type) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        T result = oserver.getByName( s_name, n_type );
        if ( null != result ) {
            fireServiceEvent( new AssetLoadEvent( this, result ) );
        }
        return result;
    }

    @Override
    public Map<AssetPath, Asset> getAssetsAlongPath(AssetPath path_asset) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        Map<AssetPath,Asset> mapResult = oserver.getAssetsAlongPath( path_asset );
        for( Asset asset : mapResult.values () ) {
            fireServiceEvent( new AssetLoadEvent( this, asset ) );
        }
        return mapResult;
    }

    @Override
    public Asset getAssetAtPath(AssetPath path_asset) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        Asset result = oserver.getAssetAtPath( path_asset );
        fireServiceEvent( new AssetLoadEvent( this, result ) );
        return result;
    }

    @Override
    public List<Asset> getAssetHistory(UUID u_id, Date t_start, Date t_end) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        // do not cache asset history
        return oserver.getAssetHistory( u_id, t_start, t_end);
    }

    @Override
    public Asset getAssetFrom(UUID u_from, String s_name) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        Asset result = oserver.getAssetFrom( u_from, s_name );
        fireServiceEvent( new AssetLoadEvent( this, result ) );
        return result;

    }

    @Override
    public Asset getAssetFromOrNull(UUID u_from, String s_name) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        Asset result = oserver.getAssetFromOrNull( u_from, s_name );
        if ( null != result ) {
            fireServiceEvent( new AssetLoadEvent( this, result ) );
        }
        return result;

    }

    @Override
    public Map<UUID, Long> checkTransactionCount(Map<UUID, Long> v_check) throws BaseException, RemoteException {
        return oserver.checkTransactionCount( v_check );
    }

    @Override
    public Set<UUID> getAssetIdsTo(UUID u_to, AssetType<? extends Asset> n_type) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return oserver.getAssetIdsTo( u_to, n_type );
    }

    @Override
    public Asset getAsset(UUID u_id) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        Asset result = oserver.getAsset( u_id );
        fireServiceEvent( new AssetLoadEvent( this, result ) );
        return result;
    }

    @Override
    public Asset getAssetOrNull(UUID u_id) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        Asset result = oserver.getAsset( u_id );
        if ( null != result ) {
            fireServiceEvent( new AssetLoadEvent( this, result ) );
        }
        return result;
    }

    @Override
    public Set<Asset> getAssets(Collection<UUID> v_id) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        Set<Asset> vResult = oserver.getAssets( v_id );
        for( Asset result : vResult ) {
            fireServiceEvent( new AssetLoadEvent( this, result ) );
        }
        return vResult;
    }

    @Override
    public Map<String, UUID> getHomeAssetIds() throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return oserver.getHomeAssetIds();
    }

    @Override
    public Map<String, UUID> getAssetIdsFrom(UUID u_from, AssetType<? extends Asset> n_type) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        final String sKey = u_from.toString() + n_type;
        final Cache<String,Object> cache = ClientCache.getSingleton().getCache();
        Map<String,UUID> mapResult = (Map<String,UUID>) cache.get( sKey );
        if ( null == mapResult ) {
            mapResult = oserver.getAssetIdsFrom( u_from, n_type );
            cache.put( sKey, mapResult );
        }
        return mapResult;
    }

    @Override
    public String getSourceName() throws RemoteException {
        return oserver.getSourceName();
    }

}
