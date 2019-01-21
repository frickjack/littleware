package littleware.asset.server.db.memory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import littleware.asset.Asset;
import littleware.asset.AssetType;
import littleware.asset.spi.AssetProviderRegistry;
import littleware.base.BaseException;
import littleware.base.UUIDFactory;
import littleware.db.DbReader;

/**
 * Load name-unique assets with the given name
 */
@SuppressWarnings("unchecked")
class DbByNameLoader implements DbReader<Optional<Asset>, String> {
    private static final Logger log = Logger.getLogger( DbByNameLoader.class.getName() );

    private final String osName;
    private final AssetType oatype;
    private final InMemLittleTransaction trans;
    private final AssetProviderRegistry assetRegistry;

    public DbByNameLoader( InMemLittleTransaction trans,
            AssetProviderRegistry assetRegistry,
            String sName, AssetType atype) {
        osName = sName;
        oatype = atype;
        this.trans = trans;
        this.assetRegistry = assetRegistry;
    }

    @Override
    public Optional<Asset> loadObject(String sIgnore) throws SQLException {
        final EntityManager entMgr = trans.getEntityManager();
        final String sQuery = "SELECT x FROM Asset x WHERE x.name=:name AND x.typeId=:typeId";
        final List<AssetEntity> vInfo = new ArrayList<AssetEntity>();
        final Query query = entMgr.createQuery(sQuery).
                setParameter("name", osName).
                setParameter("typeId", UUIDFactory.makeCleanString(oatype.getObjectId()));
        vInfo.addAll(query.getResultList());
        if (vInfo.isEmpty()) {
            final AssetTypeEntity typeEnt = entMgr.find(AssetTypeEntity.class, UUIDFactory.makeCleanString(oatype.getObjectId()));

            for (AssetTypeEntity subtype : typeEnt.getSubtypeList()) {
                vInfo.addAll(query.setParameter("typeId", subtype.getObjectId()).
                        getResultList());
                if (!vInfo.isEmpty()) {
                    break;
                }
            }
        }
        try {
            for (AssetEntity ent : vInfo) {
                final AssetType assetType = AssetType.getMember(UUIDFactory.parseUUID(ent.getTypeId()));
                log.log( Level.FINE, "Building asset from entity with type: {0}", assetType);
                return Optional.ofNullable(ent.buildAsset(assetRegistry.getService(assetType).get()));
            }
            return Optional.empty();
        } catch (BaseException ex) {
            throw new SQLException("Failed to resolve entity to asset", ex);
        }
    }
}
