package littleware.asset.server.db.jpa;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import littleware.asset.Asset;
import littleware.asset.AssetType;
import littleware.asset.IdWithClock;
import littleware.asset.server.LittleTransaction;
import littleware.asset.server.db.DbAssetManager;
import littleware.asset.spi.AssetProviderRegistry;
import littleware.db.DbReader;
import littleware.db.DbWriter;

/**
 * JPA implementation of DbAssetManager
 */
public class JpaDbAssetManager implements DbAssetManager {

    private static final Logger log = Logger.getLogger(JpaDbAssetManager.class.getName());
    private final AssetProviderRegistry assetRegistry;
    private final Provider<IdWithClock.Builder> clockBuilder;

    @Inject
    public JpaDbAssetManager(
            AssetProviderRegistry assetRegistry,
            Provider<IdWithClock.Builder> clockBuilder
            )
    {
        this.assetRegistry = assetRegistry;
        this.clockBuilder = clockBuilder;
    }


    @Override
    public DbWriter<Asset> makeDbAssetSaver( LittleTransaction trans ) {
        return new DbAssetSaver( (JpaLittleTransaction) trans );
    }

    @Override
    public DbReader<Asset, UUID> makeDbAssetLoader( LittleTransaction trans ) {
        return new DbAssetLoader( (JpaLittleTransaction) trans, assetRegistry );
    }

    @Override
    public DbWriter<Asset> makeDbAssetDeleter( LittleTransaction trans ) {
        return new DbAssetDeleter( (JpaLittleTransaction) trans );
    }

    @Override
    public DbReader<Map<String, UUID>, String> makeDbHomeIdLoader( LittleTransaction trans ) {
        return new DbHomeLoader( (JpaLittleTransaction) trans );
    }

    @Override
    public DbReader<Map<String, UUID>, String> makeDbAssetIdsFromLoader( LittleTransaction trans, UUID fromId, Optional<AssetType> maybeType, Optional<Integer> maybeState) {
        return new DbIdsFromLoader( (JpaLittleTransaction) trans, fromId, maybeType, maybeState );
    }

    @Override
    public DbReader<Set<UUID>, String> makeDbAssetIdsToLoader(LittleTransaction trans, UUID toId, Optional<AssetType> atype) {
        return new DbIdsToLoader( (JpaLittleTransaction) trans, toId, atype.orElse( null ) );
    }

    @Override
    public DbReader<Optional<Asset>, String> makeDbAssetsByNameLoader(LittleTransaction trans, String name, AssetType aType) {
        return new DbByNameLoader( (JpaLittleTransaction) trans, assetRegistry, name, aType);
    }


    @Override
    public DbReader<List<IdWithClock>, Long> makeLogLoader( LittleTransaction trans, UUID homeId ) {
        return new DbLogLoader( (JpaLittleTransaction) trans, clockBuilder.get(), homeId );
    }

    @Override
    public DbWriter<AssetType> makeTypeChecker( LittleTransaction trans ) {
        return new DbTypeChecker( (JpaLittleTransaction) trans );
    }

    @Override
    public DbReader<Optional<Asset>, String> makeDbAssetByParentLoader(LittleTransaction trans, String name, UUID parentId) {
        return new DbByParentLoader( (JpaLittleTransaction) trans, assetRegistry, name, parentId );
    }
}
