package littleware.asset.server.db.jpa;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import littleware.asset.AssetType;
import littleware.base.UUIDFactory;
import littleware.db.DbReader;

/**
 * IdsFrom database handler
 */
@SuppressWarnings("unchecked")
public class DbIdsFromLoader implements DbReader<Map<String, UUID>, String> {

    private static final Logger log = Logger.getLogger(DbIdsFromLoader.class.getName());
    private final Optional<AssetType> maybeType;
    private final Optional<Integer> maybeState;
    private final UUID uFrom;
    private final JpaLittleTransaction trans;

    public DbIdsFromLoader(JpaLittleTransaction trans,
            UUID uFrom, Optional<AssetType> maybeType, Optional<Integer> maybeState) {
        this.maybeType = maybeType;
        this.uFrom = uFrom;
        this.maybeState = maybeState;
        this.trans = trans;
    }

    /**
     * Load NameIdType info for the given type using
     * the object-property state and from-id.
     * Does not recurse to subtypes.
     * 
     * @param type to restrict assets to
     * @return list of results with type and object state under from
     */
    private List<NameIdType> loadInfo(UUID typeId) {
        final EntityManager entMgr = trans.getEntityManager();

        if ( ! maybeState.isPresent()) {
            final String sQuery = "SELECT NEW littleware.asset.server.db.jpa.NameIdType( x.name, x.objectId, x.typeId ) " +
                    "FROM Asset x WHERE x.fromId=:fromId AND x.typeId=:typeId";
            final Query query = entMgr.createQuery(sQuery).
                    setParameter("fromId", UUIDFactory.makeCleanString(uFrom)).
                    setParameter("typeId", UUIDFactory.makeCleanString(typeId));
            return query.getResultList();
        } else {
            final String sQuery = "SELECT NEW littleware.asset.server.db.jpa.NameIdType( x.name, x.objectId, x.typeId ) " +
                    "FROM Asset x WHERE x.fromId=:fromId AND x.typeId=:typeId AND x.state=:state";
            final Query query = entMgr.createQuery(sQuery).
                    setParameter("fromId", UUIDFactory.makeCleanString(uFrom)).
                    setParameter("typeId", UUIDFactory.makeCleanString(typeId)).
                    setParameter("state", maybeState.get().intValue());
            return query.getResultList();
        }
    }

    @Override
    public Map<String, UUID> loadObject(String sIgnore) throws SQLException {
        final EntityManager entMgr = trans.getEntityManager();
        final String sQuery;
        final List<NameIdType> vInfo;

        if ( ! maybeType.isPresent()) {
            sQuery = "SELECT NEW littleware.asset.server.db.jpa.NameIdType( x.name, x.objectId, x.typeId ) " +
                    "FROM Asset x WHERE x.fromId=:fromId";

            vInfo = entMgr.createQuery(sQuery).
                    setParameter("fromId", UUIDFactory.makeCleanString(uFrom)).
                    getResultList();
        } else {
            final AssetType type = maybeType.get();
            final AssetTypeEntity typeEnt = entMgr.find(AssetTypeEntity.class, UUIDFactory.makeCleanString(type.getObjectId()));
            if ( null == typeEnt ) {
                throw new IllegalStateException( "Asset type not found in database: " + type );
            }
            vInfo = loadInfo(type.getObjectId());
            for (AssetTypeEntity subtype : typeEnt.getSubtypeList()) {
                vInfo.addAll(loadInfo(UUIDFactory.parseUUID(subtype.getObjectId())));
            }
        }

        final Map<String, UUID> mapResult = new HashMap<>();
        for (NameIdType info : vInfo) {
            mapResult.put(info.getName(), info.getId());
        }

        return mapResult;
    }
}
