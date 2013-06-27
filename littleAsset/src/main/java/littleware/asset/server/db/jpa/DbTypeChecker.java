/*
 * Copyright 2011 http://code.google.com/p/littleware/
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.db.jpa;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import littleware.asset.AssetType;
import littleware.base.UUIDFactory;
import littleware.db.DbWriter;

/**
 * Verify asset-type data for given type is saved to the database.  
 */
public class DbTypeChecker implements DbWriter<AssetType> {
    private final JpaLittleTransaction trans;

    @Inject
    public DbTypeChecker( JpaLittleTransaction trans) {
        this.trans = trans;
    }

    @Override
    public void saveObject(AssetType assetType) throws SQLException {
        final List<AssetTypeEntity> empty = Collections.emptyList();
        saveAssetType(assetType, empty);
    }

    /**
     * Internal utility
     */
    private AssetTypeEntity saveAssetType(AssetType assetType, Collection<AssetTypeEntity> subType) throws SQLException {
        trans.startDbUpdate();
        try {
            final EntityManager entMgr = trans.getEntityManager();
            AssetTypeEntity entity = entMgr.find(AssetTypeEntity.class,
                    UUIDFactory.makeCleanString(assetType.getObjectId()));
            if (null == entity) {
                entity = AssetTypeEntity.buildEntity(assetType, subType);
                entMgr.merge(entity);
            } else {
                // verify subtypes
                final Set<AssetTypeEntity> existingTypes = new HashSet<AssetTypeEntity>();
                existingTypes.addAll(entity.getSubtypeList());
                if (existingTypes.addAll(subType)) {
                    entity.setSubtypeList(new ArrayList<AssetTypeEntity>(existingTypes));
                }
            }
            // Finally, verify super type
            if (assetType.getSuperType().isSet()) {
                final ImmutableList.Builder<AssetTypeEntity> builder = ImmutableList.builder();
                saveAssetType(assetType.getSuperType().get(),
                        builder.addAll(entity.getSubtypeList()).add(entity).build());
            }
            return entity;
        } finally {
            trans.endDbUpdate(false);
        }
    }
}
