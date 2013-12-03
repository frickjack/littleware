/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.client.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.asset.AssetType;
import littleware.asset.internal.RemoteSearchManager;
import littleware.base.BaseException;
import littleware.net.RemoteRetryHelper;

/**
 * Client-side RemoteSearchManager over RMI with auto-retry
 */
public class RmiSearchMgrProxy extends RemoteRetryHelper<RemoteSearchManager> implements RemoteSearchMgrProxy {

    @Inject
    public RmiSearchMgrProxy( @Named("littleware.jndi.prefix") String jndiPrefix ) {
        super(jndiPrefix + RemoteSearchManager.LOOKUP_PATH );
    }

    @Override
    public AssetResult getByName(UUID sessionId, String name, AssetType type, long cacheTimestamp) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        while (true) {
            try {
                return getLazy().getByName( sessionId, name, type, cacheTimestamp );
            } catch (RemoteException ex) {
                handle(ex);
            } catch (NullPointerException ex) {
                handle(new RemoteException("Unexpected exception", ex));
            }
        }
    }

    @Override
    public ImmutableList<Asset> getAssetHistory(UUID sessionId, UUID assetId, Date start, Date end) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        while (true) {
            try {
                return getLazy().getAssetHistory( sessionId, assetId, start, end );
            } catch (RemoteException ex) {
                handle(ex);
            } catch (NullPointerException ex) {
                handle(new RemoteException("Unexpected exception", ex));
            }
        }
    }

    @Override
    public AssetResult getAssetFrom(UUID sessionId, UUID parentId, String name, long cacheTimestamp) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        while (true) {
            try {
                return getLazy().getAssetFrom( sessionId, parentId, name, cacheTimestamp );
            } catch (RemoteException ex) {
                handle(ex);
            } catch (NullPointerException ex) {
                handle(new RemoteException("Unexpected exception", ex));
            }
        }
    }


    @Override
        public InfoMapResult getAssetIdsTo(UUID sessionId, UUID toId, AssetType type, long cacheTimestamp, int sizeInCache) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        while (true) {
            try {
                return getLazy().getAssetIdsTo( sessionId, toId, type, cacheTimestamp, sizeInCache );
            } catch (RemoteException ex) {
                handle(ex);
            } catch (NullPointerException ex) {
                handle(new RemoteException("Unexpected exception", ex));
            }
        }

    }

    @Override
    public AssetResult getAsset(UUID sessionId, UUID assetId, long cacheTimeStamp ) throws BaseException, GeneralSecurityException, RemoteException {
        while (true) {
            try {
                return getLazy().getAsset( sessionId, assetId, cacheTimeStamp );
            } catch (RemoteException ex) {
                handle(ex);
            } catch (NullPointerException ex) {
                handle(new RemoteException("Unexpected exception", ex));
            }
        }

    }

    @Override
    public ImmutableMap<UUID,AssetResult> getAssets(UUID sessionId, Map<UUID,Long> idTStampMap ) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        while (true) {
            try {
                return getLazy().getAssets( sessionId, idTStampMap );
            } catch (RemoteException ex) {
                handle(ex);
            } catch (NullPointerException ex) {
                handle(new RemoteException("Unexpected exception", ex));
            }
        }
    }

    @Override
    public InfoMapResult getHomeAssetIds(UUID sessionId, long cacheTimestamp, int sizeInCache) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        while (true) {
            try {
                return getLazy().getHomeAssetIds( sessionId, cacheTimestamp, sizeInCache );
            } catch (RemoteException ex) {
                handle(ex);
            } catch (NullPointerException ex) {
                handle(new RemoteException("Unexpected exception", ex));
            }
        }
    }

    @Override
    public InfoMapResult getAssetIdsFrom(UUID sessionId, UUID fromId, AssetType type, long cacheTimestamp, int sizeInCache) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        while (true) {
            try {
                return getLazy().getAssetIdsFrom( sessionId, fromId, type, cacheTimestamp, sizeInCache );
            } catch (RemoteException ex) {
                handle(ex);
            } catch (NullPointerException ex) {
                handle(new RemoteException("Unexpected exception", ex));
            }
        }
    }

    @Override
    public InfoMapResult getAssetIdsFrom(UUID sessionId, UUID fromId, long cacheTimestamp, int sizeInCache) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        while (true) {
            try {
                return getLazy().getAssetIdsFrom( sessionId, fromId, cacheTimestamp, sizeInCache );
            } catch (RemoteException ex) {
                handle(ex);
            } catch (NullPointerException ex) {
                handle(new RemoteException("Unexpected exception", ex));
            }
        }
    }
}
