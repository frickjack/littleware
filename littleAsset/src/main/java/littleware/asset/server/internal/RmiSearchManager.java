package littleware.asset.server.internal;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import java.util.*;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;

import littleware.asset.*;
import littleware.asset.internal.RemoteSearchManager;
import littleware.asset.server.LittleContext;
import littleware.asset.server.LittleContext.ContextFactory;
import littleware.asset.server.ServerSearchManager;
import littleware.base.*;
import static littleware.asset.internal.RemoteSearchManager.AssetResult;


/**
 * RMI remote-ready wrapper around a real implementation.
 * Should usually wrap a SubjectInvocationHandler equiped DynamicProxy
 * of a base implementation class.
 */
public class RmiSearchManager implements RemoteSearchManager {
    private static final long serialVersionUID = 3426911488683486233L;

    private final ServerSearchManager search;
    private final Cache<UUID,ServerSearchManager> sessionCache = CacheBuilder.newBuilder().softValues().build();
    private final ContextFactory contextFactory;
    
    @Inject
    public RmiSearchManager(
            ServerSearchManager proxy,
            LittleContext.ContextFactory contextFactory
            ) throws RemoteException {
        this.search = proxy;
        this.contextFactory = contextFactory;
    }

    @Override
    public AssetResult getByName(UUID sessionId, String name, AssetType type, long cacheTimestamp ) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return search.getByName( contextFactory.build( sessionId ), name, type, cacheTimestamp );
    }

    @Override
    public ImmutableList<Asset> getAssetHistory(UUID sessionId, UUID u_id, Date t_start, Date t_end)
            throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return search.getAssetHistory( contextFactory.build( sessionId ), u_id, t_start, t_end);
    }

    @Override
    public AssetResult getAsset(UUID sessionId, UUID id, long clientCacheTStamp ) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return search.getAsset( contextFactory.build( sessionId ), id, clientCacheTStamp );
    }


    @Override
    public ImmutableMap<UUID,AssetResult> getAssets(UUID sessionId, Map<UUID,Long> id2TStamp) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return search.getAssets( contextFactory.build( sessionId ), id2TStamp);
    }

    @Override
    public InfoMapResult getHomeAssetIds(UUID sessionId, long cacheTimestamp, int sizeInCache) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return search.getHomeAssetIds( contextFactory.build( sessionId ), cacheTimestamp, sizeInCache );
    }

    @Override
    public InfoMapResult getAssetIdsFrom(UUID sessionId, UUID u_from,
            AssetType n_type, long cacheTimestamp, int sizeInCache
            ) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return search.getAssetIdsFrom( contextFactory.build( sessionId ), u_from, n_type, cacheTimestamp, sizeInCache );
    }



    @Override
    public AssetResult getAssetFrom(UUID sessionId, UUID u_from, String s_name, long cacheTimestamp ) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return search.getAssetFrom( contextFactory.build( sessionId ), u_from, s_name, cacheTimestamp );
    }



    @Override
    public InfoMapResult getAssetIdsTo(UUID sessionId, UUID u_to,
            AssetType n_type, long cacheTimestamp, int sizeInCache 
            ) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return search.getAssetIdsTo( contextFactory.build( sessionId ), u_to, n_type, cacheTimestamp, sizeInCache );
    }


    @Override
    public InfoMapResult getAssetIdsFrom(UUID sessionId, UUID u_from,
        long cacheTimestamp, int sizeInCache
            ) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return search.getAssetIdsFrom( contextFactory.build( sessionId ), u_from,
                cacheTimestamp, sizeInCache
                );
    }

}
