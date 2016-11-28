package littleware.asset.client.internal;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import littleware.asset.client.spi.AssetLoadEvent;
import littleware.asset.client.spi.AssetDeleteEvent;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.asset.AssetPathFactory;
import littleware.asset.client.AssetLibrary;
import littleware.asset.client.AssetManager;
import littleware.asset.client.spi.ClientCache;
import littleware.asset.client.spi.LittleServiceBus;
import littleware.asset.internal.RemoteAssetManager;
import littleware.base.BaseException;
import littleware.security.Everybody;
import littleware.security.auth.client.KeyChain;

/**
 * Simple implementation of AssetManagerService wrapper around AssetManager
 */
public class SimpleAssetManagerService extends SimpleSearchService implements AssetManager {

    private static final long serialVersionUID = 4377427321241771838L;
    private final RemoteAssetManager server;
    private final LittleServiceBus eventBus;
    private final KeyChain keychain;
    private final AssetLibrary library;
    // hacky plugin here from SearchService - need to work this into the ClientCache
    private final SimpleSearchService.PersonalCache personalCache;

    /**
     * Inject the server to wrap with LittleService event throwing support
     */
    @Inject
    public SimpleAssetManagerService( RemoteAssetMgrProxy server, 
        LittleServiceBus eventBus,             
        ClientCache cache,
        AssetLibrary library,
        AssetPathFactory pathFactory,
        KeyChain keychain,
        Everybody everybody,
        PersonalCache personalCache
        ) {
        super( server, eventBus, cache, library, pathFactory, keychain, everybody, personalCache );
        this.server = server;
        this.eventBus = eventBus;
        this.keychain = keychain;
        this.library = library;
        this.personalCache = personalCache;
    }

    @Override
    public void deleteAsset(UUID assetId, String updateComment) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        final UUID sessionId = keychain.getDefaultSessionId().get();
        server.deleteAsset(sessionId, assetId, updateComment);
        eventBus.fireEvent(new AssetDeleteEvent(this, assetId));
    }

    @Override
    public <T extends Asset> T saveAsset(T asset, String updateComment) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        final UUID sessionId = keychain.getDefaultSessionId().get();
        final Map<UUID, Asset> result = server.saveAsset(sessionId, asset, updateComment);
        for (Asset scan : result.values()) {
            library.syncAsset(scan);
            eventBus.fireEvent(new AssetLoadEvent(this, scan));
            personalCache.put( scan );
        }
        return (T) result.get( asset.getId() );
    }

    @Override
    public Collection<Asset> saveAssetsInOrder(Collection<Asset> assetList, String updateComment) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        final UUID sessionId = keychain.getDefaultSessionId().get();
        final Map<UUID,Asset> savedAssets = server.saveAssetsInOrder(sessionId, assetList, updateComment);
        for (Asset scan : savedAssets.values() ) {
            library.syncAsset(scan);
            eventBus.fireEvent(new AssetLoadEvent(this, scan));
        }
        final ImmutableList.Builder<Asset> resultBuilder = ImmutableList.builder();
        for( Asset scan : assetList ) {
            final Asset saved = savedAssets.get( scan.getId() );
            if ( null != saved ) {
                resultBuilder.add( saved );
            }
        }
        return resultBuilder.build();
    }
}
