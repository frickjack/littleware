package littleware.asset.db.memory;

import com.google.inject.Inject;
import java.sql.SQLException;
import javax.persistence.EntityManager;
import littleware.asset.Asset;
import littleware.base.UUIDFactory;
import littleware.db.DbWriter;

/**
 * Remove an asset from the database
 */
public class DbAssetDeleter implements DbWriter<Asset> {
    private final InMemLittleTransaction trans;
    

    @Inject
    public DbAssetDeleter( InMemLittleTransaction trans ) {
        this.trans = trans;
    }


    @Override
    public void saveObject(Asset asset) throws SQLException {
        final EntityManager entMgr = trans.getEntityManager();
        final AssetEntity entity = entMgr.find( AssetEntity.class, UUIDFactory.makeCleanString( asset.getId() ) );
        entMgr.remove(entity);
    }

}
