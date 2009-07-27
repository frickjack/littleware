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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import littleware.base.Maybe;
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
    public <T extends Asset> Maybe<T> getByName(String s_name, AssetType<T> n_type) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        final Maybe<T> result = oserver.getByName( s_name, n_type );
        if ( result.isSet() ) {
            fireServiceEvent( new AssetLoadEvent( this, result.get() ) );
        }
        return result;
    }


    @Override
    public Maybe<Asset> getAssetAtPath(AssetPath path_asset) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        final Cache<String,Object> cache = getCache().getCache();
        final String sKey = "path:" + path_asset;
        Maybe<Asset> result = Maybe.emptyIfNull( (Asset) cache.get( sKey ) );
        if ( ! result.isSet() ) {
            result = oserver.getAssetAtPath( path_asset );
        }
        if ( result.isSet() ) {
            fireServiceEvent( new AssetLoadEvent( this, result.get() ) );
        }
        return result;
    }

    @Override
    public List<Asset> getAssetHistory(UUID u_id, Date t_start, Date t_end) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        // do not cache asset history
        return oserver.getAssetHistory( u_id, t_start, t_end);
    }

    @Override
    public Maybe<Asset> getAssetFrom(UUID u_from, String s_name) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        final Maybe<Asset> result = oserver.getAssetFrom( u_from, s_name );
        if ( result.isSet() ) {
            fireServiceEvent( new AssetLoadEvent( this, result.get() ) );
        }
        return result;
    }


    @Override
    public Map<UUID, Long> checkTransactionCount(Map<UUID, Long> v_check) throws BaseException, RemoteException {
        return oserver.checkTransactionCount( v_check );
    }

    @Override
    public Set<UUID> getAssetIdsTo(UUID u_to, AssetType<? extends Asset> n_type) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        final Cache<String,Object> cache = getCache().getCache();
        final String sKey = u_to.toString() + "idsTo" + n_type;
        Set<UUID> setResult = (Set<UUID>) cache.get( sKey );
        if ( null == setResult ) {
            setResult = oserver.getAssetIdsTo( u_to, n_type );
            cache.put( sKey, setResult );
        }

        return setResult;
    }

    @Override
    public Maybe<Asset> getAsset(UUID u_id) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        if ( null == u_id ) {
            return Maybe.empty();
        }
        final ClientCache cache = getCache();

        Maybe<Asset> result = Maybe.emptyIfNull( cache.get( u_id ) );
        if ( ! result.isSet() ) {
            result = oserver.getAsset( u_id );
        }
        if ( result.isSet() ) {
            fireServiceEvent( new AssetLoadEvent( this, result.get() ) );
        }
        return result;
    }


    @Override
    public List<Asset> getAssets(Collection<UUID> v_id) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        final Map<UUID,Asset>  assetMap = new HashMap<UUID,Asset>();
        final Set<UUID>   missingIds = new HashSet<UUID>();
        
        // 1st - load as much as possible from cache
        final ClientCache cache = getCache();
        for( UUID id : v_id ) {
            final Asset assetInCache = cache.get(id);
            if ( null != assetInCache ) {
                assetMap.put( id, assetInCache );
            } else {
                missingIds.add( id );
            }
        }
        final List<Asset> newAssets = oserver.getAssets( missingIds );
        for( Asset asset : newAssets ) {
            assetMap.put( asset.getObjectId(), asset );
            fireServiceEvent( new AssetLoadEvent( this, asset ) );
        }
        final List<Asset> result = new ArrayList<Asset>();
        for ( UUID id : v_id ) {
            final Asset asset = assetMap.get( id );
            if ( null != asset ) {
                result.add(asset);
            }
        }
        return result;
    }

    @Override
    public Map<String, UUID> getHomeAssetIds() throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return oserver.getHomeAssetIds();
    }

    @Override
    public Map<String, UUID> getAssetIdsFrom(UUID u_from, AssetType<? extends Asset> n_type) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        final String sKey = u_from.toString() + n_type;
        final Cache<String,Object> cache = getCache().getCache();
        Map<String,UUID> mapResult = (Map<String,UUID>) cache.get( sKey );
        if ( null == mapResult ) {
            mapResult = oserver.getAssetIdsFrom( u_from, n_type );
            cache.put( sKey, mapResult );
        }
        return mapResult;
    }

    @Override
    public Map<String, UUID> getAssetIdsFrom(UUID u_from, AssetType<? extends Asset> n_type, int i_state) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        if ( null == n_type ) {
            throw new NullPointerException( "Null type argument to getAssetIdsFrom" );
        }
        final String sKey = u_from.toString() + n_type + i_state;
        final Cache<String,Object> cache = getCache().getCache();
        Map<String,UUID> mapResult = (Map<String,UUID>) cache.get( sKey );
        if ( null == mapResult ) {
            mapResult = oserver.getAssetIdsFrom( u_from, n_type, i_state );
            cache.put( sKey, mapResult );
        }
        return mapResult;
    }

    @Override
    public Map<String, UUID> getAssetIdsFrom(UUID u_from) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return getAssetIdsFrom( u_from, null );
    }

}
