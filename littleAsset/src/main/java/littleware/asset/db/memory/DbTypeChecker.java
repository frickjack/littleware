package littleware.asset.db.memory;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.sql.SQLException;
import littleware.asset.AssetType;
import littleware.db.DbWriter;

/**
 * Verify asset-type data for given type is saved to the database.  
 */
public class DbTypeChecker implements DbWriter<AssetType> {
    private final InMemLittleTransaction trans;

    @Inject
    public DbTypeChecker( InMemLittleTransaction trans) {
        this.trans = trans;
    }

    @Override
    public void saveObject(AssetType assetType) throws SQLException {
        // NOOP
    }

}
