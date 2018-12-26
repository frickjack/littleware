package littleware.asset.client.internal;


import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import littleware.asset.client.spi.AssetLoadEvent;
import littleware.asset.client.spi.ClientCache;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.asset.AssetPath;
import littleware.asset.AssetPathByRootId;
import littleware.asset.AssetPathByRootName;
import littleware.asset.AssetPathFactory;
import littleware.asset.client.AssetSearchManager;
import littleware.asset.client.RemoteException;
import littleware.asset.AssetType;
import littleware.asset.LinkAsset;
import littleware.asset.LittleHome;
import littleware.asset.TreeChild;
import littleware.asset.TreeNode;
import littleware.asset.TreeParent;
import littleware.asset.client.AssetLibrary;
import littleware.asset.client.AssetRef;
import littleware.asset.client.spi.LittleServiceBus;
import littleware.asset.AssetInfo;
import littleware.asset.internal.RemoteSearchManager;
import littleware.asset.internal.RemoteSearchManager.AssetResult;
import littleware.asset.internal.RemoteSearchManager.InfoMapResult;
import littleware.asset.spi.AbstractAsset;
import littleware.base.BaseException;
import littleware.base.UUIDFactory;
import littleware.security.Everybody;
import littleware.security.auth.client.KeyChain;

/**
 * Smart proxy for AssetSearchManager
 * 
 * TODO - rework cacheing code - it's a mess, and the server has a new API
 */
public class SimpleSearchService implements AssetSearchManager {
    private static final Logger log = Logger.getLogger( SimpleSearchService.class.getName() );
    private static final long serialVersionUID = 1830540427533232520L;
    // Do not use final with Serializable stuff
    private final RemoteSearchManager server;
    private final LittleServiceBus eventBus;
    private final ClientCache clientCache;
    private final AssetLibrary library;
    private final AssetPathFactory pathFactory;
    private final KeyChain keychain;
    private final Everybody everybody;
    private final PersonalCache personalCache;
    
    /**
     * Internal asset cache stashes the results of various types of queries
     * that return lists of asset ids, then use that data to retrieve the
     * result from the clientCache (which has different ejection criteria).
     * For example - getAssetByName stashes an entry for its result pairing
     * the asset-name with its id, then uses that stashed entry to retrieve
     * the asset from the clientCache - verifying 
     */
    @Singleton
    public static class PersonalCache {
        private final com.google.common.cache.Cache<UUID,Asset>   personalCache = CacheBuilder.newBuilder().softValues().concurrencyLevel(4).build();  
        private final com.google.common.cache.Cache<String,UUID>  nameIdCache = CacheBuilder.newBuilder().softValues().concurrencyLevel(4).build();  
        
        private String key( AssetType type, String name ) {
            return type.toString() + "/" + name;            
        }
        
        private String key( UUID parentId, String childName ) {
            return UUIDFactory.makeCleanString(parentId) + "/" + childName;
        }
        
        public Asset get( UUID id ) { return personalCache.getIfPresent( id ); }
        public UUID get( AssetType type, String name ) {
            return nameIdCache.getIfPresent( key( type, name ) );
        }
        public void remove( AssetType type, String name ) {
            nameIdCache.invalidate( key( type, name ) );
        }
        
        public UUID get( UUID parentId, String childName ) {
            return nameIdCache.getIfPresent( key( parentId, childName ) );
        }
        public void remove( UUID parentId, String childName ) {
            nameIdCache.invalidate( key( parentId, childName ) );
        }
        
        public void put( Asset node ) {
            personalCache.put(node.getId(), node);
            if ( (! node.getAssetType().isA( LittleHome.HOME_TYPE )) && (null != node.getFromId() ) ) {
                nameIdCache.put( key( node.getFromId(), node.getName() ), node.getId() );
            }
            if ( node.getAssetType().isNameUnique() ) {
                nameIdCache.put( key( node.getAssetType(), node.getName() ), node.getId() );
            }
        }
        public void remove( UUID node ) {
            personalCache.invalidate( node );
        }
    }
    
    //----------------------------------------
    
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
            KeyChain keychain,
            Everybody everybody,
            PersonalCache personalCache
            ) {
        this.server = server;
        this.eventBus = eventBus;
        this.clientCache = cache;
        this.library = library;
        this.pathFactory = pathFactory;
        this.keychain = keychain;
        this.everybody = everybody;
        this.personalCache = personalCache;
    }

    @Override
    public AssetRef getByName(String name, AssetType assetType) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        { // check cache
          final UUID cacheId = personalCache.get( assetType, name );
          if ( null != cacheId ) {
              final AssetRef ref = getAsset( cacheId );
              if ( ref.isPresent() && ref.get().getName().equals( name ) && ref.get().getAssetType().isA( assetType ) ) {
                  return ref;
              } else {
                  personalCache.remove( assetType, name );
              }
          }
        }
        
        final UUID sessionId = keychain.getDefaultSessionId().get();
        final AssetResult result = server.getByName(sessionId, name, assetType, -1L );
        if (result.getAsset().isPresent()) {
            final Asset asset = result.getAsset().get();
            personalCache.put( asset );
            eventBus.fireEvent(new AssetLoadEvent(this, asset ));
            return library.syncAsset(asset);
        }
        return AssetRef.EMPTY;
    }

    @Override
    public AssetRef getAssetAtPath(AssetPath path) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        final String key = "path:" + path;
        {
            final Optional<Asset> cacheEntry = Optional.ofNullable( clientCache.get(key));
            if (cacheEntry.isPresent()) {
                return library.syncAsset(cacheEntry.get());
            }
        }
        final UUID sessionId = keychain.getDefaultSessionId().get();
        if (path.hasRootBacktrack()) {
            final AssetRef result = getAssetAtPath(normalizePath(path));
            if (result.isPresent()) {
                clientCache.put(key, result.get());
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
                result.isPresent()
                && result.get().getAssetType().isA(LinkAsset.LINK_TYPE)
                && (((AbstractAsset) result.get()).getToId() != null);
                ++i_link_count) {
            if (i_link_count > 5) {
                throw new IllegalArgumentException("Traversal exceeded 5 link limit at " + pathString);
            }
            result = getAsset(((AbstractAsset) result.get()).getToId());
        }

        if (result.isPresent()) {
            clientCache.put(key, result.get());
            eventBus.fireEvent(new AssetLoadEvent(this, result.get()));
            return library.syncAsset(result.get());
        }
        return AssetRef.EMPTY;
    }

    @Override
    public ImmutableList<Asset> getAssetHistory(UUID id, Date start,
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
        final String key = "from:" + UUIDFactory.makeCleanString(parentId) + name;
        Optional<Asset> result = Optional.ofNullable( clientCache.get(key));
        if (result.isPresent()) {
            return library.syncAsset(result.get());
        }
        
        { // Try the nameIdCache - it doesn't get flushed, and leverages the server-timestamp stuff
            final UUID id = personalCache.get( parentId, name);
            if ( null != id ) {
                final AssetRef ref = getAsset( id );
                if ( ref.isPresent() && parentId.equals( ref.get().getFromId() ) && name.equals( ref.get().getName() ) ) {
                    return ref;
                } else {
                    personalCache.remove( parentId, name );
                }
            }
        }
        final UUID sessionId = keychain.getDefaultSessionId().get();
        final AssetResult serverResult = server.getAssetFrom(sessionId, parentId, name, -1 );
        result = server.getAssetFrom(sessionId, parentId, name, -1 ).getAsset();
        if (result.isPresent()) {
            eventBus.fireEvent(new AssetLoadEvent(this, result.get()));
            // result gets indexed multiple ways - ugh!
            clientCache.put(key, result.get());
            personalCache.put( result.get() );
            return library.syncAsset(result.get());
        }
        return AssetRef.EMPTY;
    }


    @Override
    public ImmutableMap<String,AssetInfo> getAssetIdsTo(UUID toId, AssetType assetType)
            throws BaseException,
            AssetException,
            GeneralSecurityException,
            RemoteException {
        final String sKey = toId.toString() + "idsTo" + assetType;
        final UUID sessionId = keychain.getDefaultSessionId().get();
        final InfoMapResult serverResult = server.getAssetIdsTo(sessionId, toId, assetType, -1, 0);
        final ImmutableMap<String, AssetInfo> result = serverResult.getData();
        return result;
    }

    @Override
    public AssetRef getAsset(UUID id) throws BaseException,
            AssetException,
            GeneralSecurityException,
            RemoteException {
        if (null == id) {
            return AssetRef.EMPTY;
        }
        if ( id.equals( everybody.getId() ) ) {
            // Shortcut everybody-group access
            return library.syncAsset( everybody );
        }

        Optional<Asset> result = Optional.ofNullable(clientCache.get(id));
        if (result.isPresent()) {
            return library.syncAsset(result.get());
        }
        result = Optional.ofNullable( personalCache.get(id));
        final UUID sessionId = keychain.getDefaultSessionId().get();
        final long timestamp = (result.isPresent()) ? result.get().getTimestamp() : -1L;
        {
            final AssetResult serverInfo = server.getAsset( sessionId, id, timestamp );
            if ( ! serverInfo.getState().equals( AssetResult.State.USE_YOUR_CACHE )) {
                result = serverInfo.getAsset();
                if( result.isPresent() ) {
                    personalCache.put(result.get() );
                } else {
                    personalCache.remove(id);
                }
            } else {
                //log.log( Level.FINE, "server-cache hit ..." );
            }
        } 

        if (result.isPresent()) {
            eventBus.fireEvent(new AssetLoadEvent(this, result.get()));
            return library.syncAsset(result.get());
        }
        return AssetRef.EMPTY;
    }

    @Override
    public ImmutableMap<UUID,AssetRef> getAssets(Collection<UUID> idList) throws BaseException,
            AssetException,
            GeneralSecurityException,
            RemoteException {
        final Map<UUID, Asset> assetMap = new HashMap<>();
        final Map<UUID,Long> missingIds = new HashMap<>();

        // 1st - load as much as possible from cache
        for (UUID id : idList) {
            final Asset assetInCache = clientCache.get(id);
            if (null != assetInCache) {
                assetMap.put(id, assetInCache);
            } else {
                final Asset cachedAsset = personalCache.get(id);
                final long timestamp = (null == cachedAsset) ? -1L : cachedAsset.getTimestamp();
                missingIds.put( id, timestamp);
                if ( null != cachedAsset ) {
                    assetMap.put( id, cachedAsset );
                }
            }
        }
        if (!missingIds.isEmpty()) {
            final UUID sessionId = keychain.getDefaultSessionId().get();
            final Map<UUID,AssetResult> newAssets = server.getAssets(sessionId, missingIds);
            for ( Map.Entry<UUID,AssetResult> entry  : newAssets.entrySet() ) {
                final AssetResult lookupResult = entry.getValue();
                if ( lookupResult.getAsset().isPresent() ) {
                    final Asset asset = lookupResult.getAsset().get();
                    assetMap.put(asset.getId(), asset);
                    personalCache.put( asset );
                    eventBus.fireEvent(new AssetLoadEvent(this, asset));
                } else if ( lookupResult.getState().equals( AssetResult.State.USE_YOUR_CACHE ) ) {
                    final Asset asset = assetMap.get( entry.getKey() );
                    eventBus.fireEvent(new AssetLoadEvent(this, asset));
                } else {
                    personalCache.remove( entry.getKey() );
                    assetMap.remove( entry.getKey() );
                }
            }
        }
        
        final ImmutableMap.Builder<UUID,AssetRef> resultBuilder = ImmutableMap.builder();
        for (UUID id : idList) {
            final Asset asset = assetMap.get(id);
            if (null != asset) {
                resultBuilder.put(asset.getId(), this.library.syncAsset(asset));
            }
        }
        return resultBuilder.build();
    }

    @Override
    public ImmutableMap<String, AssetInfo> getHomeAssetIds() throws BaseException,
            AssetException,
            GeneralSecurityException,
            RemoteException {
        final UUID sessionId = keychain.getDefaultSessionId().get();
        return server.getHomeAssetIds(sessionId, -1, 0 ).getData();
    }

    @Override
    public ImmutableMap<String, AssetInfo> getAssetIdsFrom(UUID parentId, AssetType assetType)
            throws BaseException,
            AssetException,
            GeneralSecurityException,
            RemoteException {
        final String key = parentId.toString() + assetType;
        // TODO - re-introduce cache here
        final UUID sessionId = keychain.getDefaultSessionId().get();
        final InfoMapResult serverResult = server.getAssetIdsFrom(sessionId, parentId, assetType, -1, 0 );
        return serverResult.getData();
    }

    @Override
    public ImmutableMap<String, AssetInfo> getAssetIdsFrom(UUID parentId) throws BaseException,
            AssetException,
            GeneralSecurityException,
            RemoteException {
        return getAssetIdsFrom(parentId, null);
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
            if (!maybeParent.isPresent()) {
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
        if ((!maybeRoot.isPresent()) || ((!(maybeRoot.get() instanceof TreeParent))
                && (!(maybeRoot.get() instanceof TreeChild)))) {
            return pathNormal;
        }
        final List<Asset> assetTrail = new ArrayList<Asset>();
        assetTrail.add(maybeRoot.get());
        for (AssetRef maybeParent = getAsset(maybeRoot.get().getFromId());
                maybeParent.isPresent();
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
