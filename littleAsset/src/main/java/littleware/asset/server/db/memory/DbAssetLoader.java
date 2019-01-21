/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.db.memory;

import com.google.inject.Inject;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import littleware.asset.Asset;
import littleware.asset.AssetType;
import littleware.asset.spi.AssetProviderRegistry;
import littleware.base.BaseException;
import littleware.base.UUIDFactory;
import littleware.db.DbReader;

public class DbAssetLoader implements DbReader<Asset, UUID> {

    private static final Logger log = Logger.getLogger(DbAssetLoader.class.getName());
    private final InMemLittleTransaction trans;

    private final AssetProviderRegistry assetRegistry;

    @Inject
    public DbAssetLoader( InMemLittleTransaction trans,
            AssetProviderRegistry assetRegistry) {
        this.trans = trans;
        this.assetRegistry = assetRegistry;
    }

    @Override
    public Asset loadObject(UUID id) throws SQLException {
        final EntityManager entMgr = trans.getEntityManager();
        final AssetEntity ent = entMgr.find(AssetEntity.class,
                UUIDFactory.makeCleanString(id));
        try {
            if (null != ent) {
                final AssetType assetType = AssetType.getMember(UUIDFactory.parseUUID(ent.getTypeId()));
                return ent.buildAsset(assetRegistry.getService(assetType).get());
            } else {
                return null;
            }
        } catch (BaseException ex) {
            throw new SQLException("Failed data load for " + id, ex);
        }
    }
}
