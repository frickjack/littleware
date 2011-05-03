/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.client.internal;

import com.google.inject.Inject;
import littleware.asset.client.spi.AssetLoadEvent;
import littleware.asset.client.spi.ClientCache;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import littleware.asset.AssetPathByRootId;
import littleware.asset.AssetPathByRootName;
import littleware.asset.AssetPathFactory;
import littleware.asset.client.AssetSearchManager;
import littleware.asset.AssetType;
import littleware.asset.IdWithClock;
import littleware.asset.LinkAsset;
import littleware.asset.TreeNode;
import littleware.asset.TreeParent;
import littleware.asset.client.AssetLibrary;
import littleware.asset.client.AssetRef;
import littleware.asset.client.spi.LittleServiceBus;
import littleware.asset.internal.RemoteSearchManager;
import littleware.asset.spi.AbstractAsset;
import littleware.base.BaseException;
import littleware.base.cache.Cache;
import littleware.base.Maybe;
import littleware.base.Option;
import littleware.base.UUIDFactory;
import littleware.security.auth.client.KeyChain;

/**
 * Smart proxy for AssetSearchManager
 */
public class SimpleSearchService implements AssetSearchManager {

    private static final long serialVersionUID = 1830540427533232520L;
    // Do not use final with Serializable stuff
    private final RemoteSearchManager server;
    private final LittleServiceBus eventBus;
    private final ClientCache clientCache;
    private final AssetLibrary library;
    private final AssetPathFactory pathFactory;
    private final KeyChain keychain;

    /**
     * Inject the server that does not implement LittleService event support
     *
     * @param server to wrap with events
     */
    @Inject
    public SimpleSearchService(
            RemoteSearchManager server,
            LittleServiceBus eventBus,
            ClientCache cache,
            AssetLibrary library,
            AssetPathFactory pathFactory,
            KeyChain keychain) {
        this.server = server;
        this.eventBus = eventBus;
        this.clientCache = cache;
        this.library = library;
        this.pathFactory = pathFactory;
        this.keychain = keychain;
    }

    @Override
    public AssetRef getByName(String name, AssetType assetType) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        final UUID sessionId = keychain.getDefaultSessionId().get();
        final Option<Asset> result = server.getByName(sessionId, name, assetType);
        if (result.isSet()) {
            eventBus.fireEvent(new AssetLoadEvent(this, result.get()));
            return library.syncAsset(result.get());
        }
        return AssetRef.EMPTY;
    }

    @Override
    public AssetRef getAssetAtPath(AssetPath path) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        final Cache<String, Object> cache = clientCache.getCache();
        final String key = "path:" + path;
        {
            final Option<Asset> cacheEntry = Maybe.emptyIfNull((Asset) cache.get(key));
            if (cacheEntry.isSet()) {
                return library.syncAsset(cacheEntry.get());
            }
        }
        final UUID sessionId = keychain.getDefaultSessionId().get();
        if (path.hasRootBacktrack()) {
            final AssetRef result = getAssetAtPath(normalizePath(path));
            if (result.isSet()) {
                cache.put(key, result.getRef());
            }
            return result;
        }

        final String pathString = path.toString();
        AssetRef result = AssetRef.EMPTY;
        if (path.hasParent()) {
            // else get parent
            // recursion!
            final AssetRef parentRef = getAssetAtPath(path.getParent());
            if (parentRef.isEmpty()) {
                return parentRef;
            }
            final String name = pathString.substring(pathString.lastIndexOf("/") + 1);
            result = getAssetFrom(parentRef.get().getId(), name);
        } else {
            result = this.getRoot(path);
        }


        for (int i_link_count = 0;
                result.isSet()
                && result.get().getAssetType().isA(LinkAsset.LINK_TYPE)
                && (((AbstractAsset) result.get()).getToId() != null);
                ++i_link_count) {
            if (i_link_count > 5) {
                throw new IllegalArgumentException("Traversal exceeded 5 link limit at " + pathString);
            }
            result = getAsset(((AbstractAsset) result.get()).getToId());
        }


        if (result.isSet()) {
            cache.put(key, result.get());
            eventBus.fireEvent(new AssetLoadEvent(this, result.get()));
            return library.syncAsset(result.get());
        }
        return AssetRef.EMPTY;
    }

    @Override
    public List<Asset> getAssetHistory(UUID id, Date start,
            Date end) throws BaseException,
            AssetException,
            GeneralSecurityException,
            RemoteException {
        // do not cache asset history
        final UUID sessionId = keychain.getDefaultSessionId().get();
        return server.getAssetHistory(sessionId, id, start, end);
    }

    @Override
    public AssetRef getAssetFrom(UUID parentId, String name)
            throws BaseException,
            AssetException,
            GeneralSecurityException,
            RemoteException {
        final Cache<String, Object> cache = clientCache.getCache();
        final String key = "from:" + UUIDFactory.makeCleanString(parentId) + name;
        Option<Asset> result = Maybe.emptyIfNull((Asset) cache.get(key));
        if (result.isSet()) {
            return library.syncAsset(result.get());
        }
        final UUID sessionId = keychain.getDefaultSessionId().get();
        result = server.getAssetFrom(sessionId, parentId, name);
        if (result.isSet()) {
            eventBus.fireEvent(new AssetLoadEvent(this, result.get()));
            cache.put(key, result.get());
            return library.syncAsset(result.get());
        }
        return AssetRef.EMPTY;
    }

    @Override
    public Map<UUID, Long> checkTransactionCount(Map<UUID, Long> checkMap) throws BaseException,
            RemoteException {
        final UUID sessionId = keychain.getDefaultSessionId().get();
        return server.checkTransactionCount(sessionId, checkMap);
    }

    @Override
    public Set<UUID> getAssetIdsTo(UUID toId, AssetType assetType)
            throws BaseException,
            AssetException,
            GeneralSecurityException,
            RemoteException {
        final Cache<String, Object> cache = clientCache.getCache();
        final String sKey = toId.toString() + "idsTo" + assetType;
        Set<UUID> setResult = (Set<UUID>) cache.get(sKey);
        if (null == setResult) {
            final UUID sessionId = keychain.getDefaultSessionId().get();
            setResult = server.getAssetIdsTo(sessionId, toId, assetType);
            cache.put(sKey, setResult);
        }
        return setResult;
    }

    @Override
    public AssetRef getAsset(UUID id) throws BaseException,
            AssetException,
            GeneralSecurityException,
            RemoteException {
        if (null == id) {
            return AssetRef.EMPTY;
        }

        Option<Asset> result = Maybe.emptyIfNull(clientCache.get(id));
        if (result.isSet()) {
            return library.syncAsset(result.get());
        }
        final UUID sessionId = keychain.getDefaultSessionId().get();
        result = server.getAsset(sessionId, id);

        if (result.isSet()) {
            eventBus.fireEvent(new AssetLoadEvent(this, result.get()));
            return library.syncAsset(result.get());
        }
        return AssetRef.EMPTY;
    }

    @Override
    public List<Asset> getAssets(Collection<UUID> idList) throws BaseException,
            AssetException,
            GeneralSecurityException,
            RemoteException {
        final Map<UUID, Asset> assetMap = new HashMap<UUID, Asset>();
        final Set<UUID> missingIds = new HashSet<UUID>();

        // 1st - load as much as possible from cache
        for (UUID id : idList) {
            final Asset assetInCache = clientCache.get(id);
            if (null != assetInCache) {
                assetMap.put(id, assetInCache);
            } else {
                missingIds.add(id);
            }
        }
        if (!missingIds.isEmpty()) {
            final UUID sessionId = keychain.getDefaultSessionId().get();
            final List<Asset> newAssets = server.getAssets(sessionId, missingIds);
            for (Asset asset : newAssets) {
                assetMap.put(asset.getId(), asset);
                eventBus.fireEvent(new AssetLoadEvent(this, asset));
            }
        }
        final List<Asset> result = new ArrayList<Asset>();
        for (UUID id : idList) {
            final Asset asset = assetMap.get(id);
            if (null != asset) {
                result.add(asset);
            }
        }
        return result;
    }

    @Override
    public Map<String, UUID> getHomeAssetIds() throws BaseException,
            AssetException,
            GeneralSecurityException,
            RemoteException {
        final UUID sessionId = keychain.getDefaultSessionId().get();
        return server.getHomeAssetIds(sessionId);
    }

    @Override
    public Map<String, UUID> getAssetIdsFrom(UUID parentId, AssetType assetType)
            throws BaseException,
            AssetException,
            GeneralSecurityException,
            RemoteException {
        final String key = parentId.toString() + assetType;
        final Cache<String, Object> cache = clientCache.getCache();
        Map<String, UUID> mapResult = (Map<String, UUID>) cache.get(key);
        if (null == mapResult) {
            final UUID sessionId = keychain.getDefaultSessionId().get();
            mapResult = server.getAssetIdsFrom(sessionId, parentId, assetType);
            cache.put(key, mapResult);
        }
        return mapResult;
    }

    @Override
    public Map<String, UUID> getAssetIdsFrom(UUID parentId) throws BaseException,
            AssetException,
            GeneralSecurityException,
            RemoteException {
        return getAssetIdsFrom(parentId, null);
    }

    @Override
    public List<IdWithClock> checkTransactionLog(UUID homeId, long minTransaction) throws BaseException,
            RemoteException {
        final UUID sessionId = keychain.getDefaultSessionId().get();
        return server.checkTransactionLog(sessionId, homeId, minTransaction);
    }

    public AssetRef getRoot(AssetPath pathIn) throws BaseException, GeneralSecurityException, RemoteException {
        if (pathIn instanceof AssetPathByRootId) {
            return getAsset(((AssetPathByRootId) pathIn).getRootId());
        } else {
            final AssetPathByRootName path = (AssetPathByRootName) pathIn;
            return getByName(path.getRootName(), path.getRootType());
        }
    }

    @Override
    public AssetPath normalizePath(final AssetPath pathIn) throws BaseException, AssetException, GeneralSecurityException,
            RemoteException {
        String normalStr = pathIn.getSubRootPath();
        if (normalStr.startsWith("..")) { // just in case
            normalStr = "/" + normalStr;
        }
        if (!normalStr.startsWith("/..")) {
            return pathIn;
        }
        Asset rootAsset = getRoot(pathIn).get();
        for (;
                normalStr.startsWith("/..");
                normalStr = normalStr.substring(3)) {
            TreeNode treeNode = rootAsset.narrow();
            final AssetRef maybeParent = getAsset(treeNode.getParentId());
            if (!maybeParent.isSet()) {
                throw new IllegalArgumentException("Unable to normalize path for " + pathIn);
            }
            rootAsset = maybeParent.get();
        }
        return pathFactory.createPath(rootAsset.getId(), normalStr);
    }

    @Override
    public AssetPath toRootedPath(AssetPath pathIn) throws BaseException, GeneralSecurityException,
            RemoteException {
        final AssetPath pathNormal = normalizePath(pathIn);
        final AssetRef maybeRoot = getRoot(pathNormal);
        if ((!maybeRoot.isSet()) || (!(maybeRoot.get() instanceof TreeParent))) {
            return pathNormal;
        }
        final List<Asset> assetTrail = new ArrayList<Asset>();
        assetTrail.add(maybeRoot.get());
        for (AssetRef maybeParent = getAsset(maybeRoot.get().getFromId());
                maybeParent.isSet();
                maybeParent = getAsset(maybeParent.get().getFromId())) {
            assetTrail.add(maybeParent.get());
        }
        Collections.reverse(assetTrail);
        final StringBuilder sbSubrootPath = new StringBuilder();
        boolean bFirst = true;
        for (Asset aPart : assetTrail) {
            if (bFirst) {
                // skip the root
                bFirst = false;
                continue;
            }
            sbSubrootPath.append("/").append(aPart.getName());
        }
        sbSubrootPath.append("/").append(pathNormal.getSubRootPath());
        final Asset aRoot = assetTrail.get(0);
        if (aRoot.getAssetType().isNameUnique()) {
            return normalizePath(pathFactory.createPath(aRoot.getName(), aRoot.getAssetType(), sbSubrootPath.toString()));
        } else {
            return normalizePath(pathFactory.createPath(aRoot.getId(), sbSubrootPath.toString()));
        }
    }

    @Override
    public AssetPath toRootedPath(UUID assetId) throws BaseException, GeneralSecurityException, RemoteException {
        return toRootedPath(pathFactory.createPath(assetId));
    }
}
