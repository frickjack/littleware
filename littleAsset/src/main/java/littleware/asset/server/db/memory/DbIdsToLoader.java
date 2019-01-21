package littleware.asset.server.db.memory;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import littleware.asset.AssetType;
import littleware.base.UUIDFactory;
import littleware.db.DbReader;

/**
 * JPA handler loads list of asset-ids linking to a given asset id
 */
@SuppressWarnings("unchecked")
public class DbIdsToLoader implements DbReader<Set<UUID>,String> {
    private final AssetType assetType;
    private final UUID toId;
    private final InMemLittleTransaction trans;

    public DbIdsToLoader( InMemLittleTransaction trans,
            UUID uTo, AssetType atype ) {
        assetType = atype;
        toId = uTo;
        this.trans = trans;
    }

    @Override
    public Set<UUID> loadObject(String sIgnore) {
        throw new UnsupportedOperationException();
    }

}
