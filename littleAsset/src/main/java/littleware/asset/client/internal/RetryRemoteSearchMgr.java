/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.client.internal;

import com.google.inject.Inject;
import com.google.inject.name.Named;
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
import littleware.asset.AssetType;
import littleware.asset.IdWithClock;
import littleware.asset.internal.RemoteSearchManager;
import littleware.base.BaseException;
import littleware.base.Option;
import littleware.net.RemoteRetryHelper;

/**
 * Client-side RemoteSearchManager over RMI with auto-retry
 */
public class RetryRemoteSearchMgr extends RemoteRetryHelper<RemoteSearchManager> implements RemoteSearchManager {

    @Inject
    public RetryRemoteSearchMgr( @Named("littleware.jndi.prefix") String jndiPrefix ) {
        super(jndiPrefix + RemoteSearchManager.LOOKUP_PATH );
    }

    @Override
    public Option<Asset> getByName(UUID sessionId, String name, AssetType type) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        while (true) {
            try {
                return getLazy().getByName( sessionId, name, type );
            } catch (RemoteException ex) {
                handle(ex);
            } catch (NullPointerException ex) {
                handle(new RemoteException("Unexpected exception", ex));
            }
        }
    }

    @Override
    public List<Asset> getAssetHistory(UUID sessionId, UUID assetId, Date start, Date end) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
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
    public Option<Asset> getAssetFrom(UUID sessionId, UUID parentId, String name) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        while (true) {
            try {
                return getLazy().getAssetFrom( sessionId, parentId, name );
            } catch (RemoteException ex) {
                handle(ex);
            } catch (NullPointerException ex) {
                handle(new RemoteException("Unexpected exception", ex));
            }
        }
    }

    @Override
    public Map<UUID, Long> checkTransactionCount(UUID sessionId, Map<UUID, Long> checkMap) throws BaseException, RemoteException {
        while (true) {
            try {
                return getLazy().checkTransactionCount( sessionId, checkMap );
            } catch (RemoteException ex) {
                handle(ex);
            } catch (NullPointerException ex) {
                handle(new RemoteException("Unexpected exception", ex));
            }
        }
    }

    @Override
    public List<IdWithClock> checkTransactionLog(UUID sessionId, UUID homeId, long minTransaction) throws BaseException, RemoteException {
        while (true) {
            try {
                return getLazy().checkTransactionLog( sessionId, homeId, minTransaction );
            } catch (RemoteException ex) {
                handle(ex);
            } catch (NullPointerException ex) {
                handle(new RemoteException("Unexpected exception", ex));
            }
        }
    }

    @Override
    public Set<UUID> getAssetIdsTo(UUID sessionId, UUID toId, AssetType type) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        while (true) {
            try {
                return getLazy().getAssetIdsTo( sessionId, toId, type );
            } catch (RemoteException ex) {
                handle(ex);
            } catch (NullPointerException ex) {
                handle(new RemoteException("Unexpected exception", ex));
            }
        }

    }

    @Override
    public Option<Asset> getAsset(UUID sessionId, UUID assetId) throws BaseException, GeneralSecurityException, RemoteException {
        while (true) {
            try {
                return getLazy().getAsset( sessionId, assetId );
            } catch (RemoteException ex) {
                handle(ex);
            } catch (NullPointerException ex) {
                handle(new RemoteException("Unexpected exception", ex));
            }
        }

    }

    @Override
    public List<Asset> getAssets(UUID sessionId, Collection<UUID> idSet) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        while (true) {
            try {
                return getLazy().getAssets( sessionId, idSet );
            } catch (RemoteException ex) {
                handle(ex);
            } catch (NullPointerException ex) {
                handle(new RemoteException("Unexpected exception", ex));
            }
        }
    }

    @Override
    public Map<String, UUID> getHomeAssetIds(UUID sessionId) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        while (true) {
            try {
                return getLazy().getHomeAssetIds( sessionId );
            } catch (RemoteException ex) {
                handle(ex);
            } catch (NullPointerException ex) {
                handle(new RemoteException("Unexpected exception", ex));
            }
        }
    }

    @Override
    public Map<String, UUID> getAssetIdsFrom(UUID sessionId, UUID fromId, AssetType type) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        while (true) {
            try {
                return getLazy().getAssetIdsFrom( sessionId, fromId, type );
            } catch (RemoteException ex) {
                handle(ex);
            } catch (NullPointerException ex) {
                handle(new RemoteException("Unexpected exception", ex));
            }
        }
    }

    @Override
    public Map<String, UUID> getAssetIdsFrom(UUID sessionId, UUID fromId) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        while (true) {
            try {
                return getLazy().getAssetIdsFrom( sessionId, fromId );
            } catch (RemoteException ex) {
                handle(ex);
            } catch (NullPointerException ex) {
                handle(new RemoteException("Unexpected exception", ex));
            }
        }
    }
}
