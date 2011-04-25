/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.client.internal;

import littleware.asset.client.spi.AssetLoadEvent;
import littleware.asset.client.spi.ClientCache;
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
import littleware.asset.IdWithClock;
import littleware.asset.client.AssetSearchService;
import littleware.asset.client.spi.LittleServiceBus;
import littleware.base.BaseException;
import littleware.base.cache.Cache;
import littleware.base.Maybe;

/**
 * Smart proxy for AssetSearchManager
 */
public class SimpleAssetSearchService implements AssetSearchService {
    private static final long serialVersionUID = 1830540427533232520L;

    // Do not use final with Serializable stuff
    private AssetSearchManager     server;
    private final LittleServiceBus eventBus;
    private final ClientCache      clientCache;

    /**
     * Inject the server that does not implement LittleService event support
     *
     * @param server to wrap with events
     */
    public SimpleAssetSearchService( AssetSearchManager server,
            LittleServiceBus eventBus,
            ClientCache cache ) {
        this.server = server;
        if ( this.server instanceof AssetSearchService ) {
            throw new IllegalArgumentException( "Attempt to double wrap LittleService smart proxy" );
        }
        this.eventBus = eventBus;
        this.clientCache = cache;
    }

    @Override
    public Maybe<Asset> getByName(String s_name, AssetType n_type) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        final Maybe<Asset> result = server.getByName( s_name, n_type );
        if ( result.isSet() ) {
            eventBus.fireEvent( new AssetLoadEvent( this, result.get() ) );
        }
        return result;
    }


    @Override
    public Maybe<Asset> getAssetAtPath(AssetPath path_asset) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        final Cache<String,Object> cache = clientCache.getCache();
        final String sKey = "path:" + path_asset;
        Maybe<Asset> result = Maybe.emptyIfNull( (Asset) cache.get( sKey ) );
        if ( ! result.isSet() ) {
            result = server.getAssetAtPath( path_asset );
        }
        if ( result.isSet() ) {
            cache.put( sKey, result.get() );
            eventBus.fireEvent( new AssetLoadEvent( this, result.get() ) );
        }
        return result;
    }

    @Override
    public List<Asset> getAssetHistory(UUID u_id, Date t_start, Date t_end) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        // do not cache asset history
        return server.getAssetHistory( u_id, t_start, t_end);
    }

    @Override
    public Maybe<Asset> getAssetFrom(UUID u_from, String s_name) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        final Maybe<Asset> result = server.getAssetFrom( u_from, s_name );
        if ( result.isSet() ) {
            eventBus.fireEvent( new AssetLoadEvent( this, result.get() ) );
        }
        return result;
    }


    @Override
    public Map<UUID, Long> checkTransactionCount(Map<UUID, Long> v_check) throws BaseException, RemoteException {
        return server.checkTransactionCount( v_check );
    }

    @Override
    public Set<UUID> getAssetIdsTo(UUID u_to, AssetType n_type) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        final Cache<String,Object> cache = clientCache.getCache();
        final String sKey = u_to.toString() + "idsTo" + n_type;
        Set<UUID> setResult = (Set<UUID>) cache.get( sKey );
        if ( null == setResult ) {
            setResult = server.getAssetIdsTo( u_to, n_type );
            cache.put( sKey, setResult );
        }

        return setResult;
    }

    @Override
    public Maybe<Asset> getAsset(UUID u_id) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        if ( null == u_id ) {
            return Maybe.empty();
        }

        Maybe<Asset> result = Maybe.emptyIfNull( clientCache.get( u_id ) );
        if ( ! result.isSet() ) {
            result = server.getAsset( u_id );
        }
        if ( result.isSet() ) {
            eventBus.fireEvent( new AssetLoadEvent( this, result.get() ) );
        }
        return result;
    }


    @Override
    public List<Asset> getAssets(Collection<UUID> v_id) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        final Map<UUID,Asset>  assetMap = new HashMap<UUID,Asset>();
        final Set<UUID>   missingIds = new HashSet<UUID>();
        
        // 1st - load as much as possible from cache
        for( UUID id : v_id ) {
            final Asset assetInCache = clientCache.get(id);
            if ( null != assetInCache ) {
                assetMap.put( id, assetInCache );
            } else {
                missingIds.add( id );
            }
        }
        final List<Asset> newAssets = server.getAssets( missingIds );
        for( Asset asset : newAssets ) {
            assetMap.put( asset.getId(), asset );
            eventBus.fireEvent( new AssetLoadEvent( this, asset ) );
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
        return server.getHomeAssetIds();
    }

    @Override
    public Map<String, UUID> getAssetIdsFrom(UUID u_from, AssetType n_type) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        final String sKey = u_from.toString() + n_type;
        final Cache<String,Object> cache = clientCache.getCache();
        Map<String,UUID> mapResult = (Map<String,UUID>) cache.get( sKey );
        if ( null == mapResult ) {
            mapResult = server.getAssetIdsFrom( u_from, n_type );
            cache.put( sKey, mapResult );
        }
        return mapResult;
    }

    @Override
    public Map<String, UUID> getAssetIdsFrom(UUID u_from, AssetType n_type, int i_state) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        if ( null == n_type ) {
            throw new NullPointerException( "Null type argument to getAssetIdsFrom" );
        }
        final String sKey = u_from.toString() + n_type + i_state;
        final Cache<String,Object> cache = clientCache.getCache();
        Map<String,UUID> mapResult = (Map<String,UUID>) cache.get( sKey );
        if ( null == mapResult ) {
            mapResult = server.getAssetIdsFrom( u_from, n_type, i_state );
            cache.put( sKey, mapResult );
        }
        return mapResult;
    }

    @Override
    public Map<String, UUID> getAssetIdsFrom(UUID u_from) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return getAssetIdsFrom( u_from, null );
    }

    @Override
    public List<IdWithClock> checkTransactionLog(UUID homeId, long minTransaction) throws BaseException, RemoteException {
        return server.checkTransactionLog(homeId, minTransaction);
    }

}
