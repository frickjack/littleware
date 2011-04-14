/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.db.jpa;

import com.google.inject.Provider;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.asset.AssetType;
import littleware.base.UUIDFactory;
import littleware.db.DbReader;

/**
 * Load name-unique assets with the given name
 */
class DbByNameLoader implements DbReader<Set<Asset>, String> {

    private final String osName;
    private final AssetType oatype;
    private final Provider<JpaLittleTransaction> oprovideTrans;

    public DbByNameLoader(Provider<JpaLittleTransaction> provideTrans, String sName, AssetType atype) {
        osName = sName;
        oatype = atype;
        oprovideTrans = provideTrans;
    }

    @Override
    public Set<Asset> loadObject(String sIgnore) throws SQLException {
        final EntityManager entMgr = oprovideTrans.get().getEntityManager();
        final String sQuery = "SELECT x FROM Asset x WHERE x.name=:name AND x.typeId=:typeId";
        final List<AssetEntity> vInfo = new ArrayList<AssetEntity>();
        final Query query = entMgr.createQuery(sQuery).
                setParameter("name", osName).
                setParameter("typeId", UUIDFactory.makeCleanString(oatype.getObjectId()));
        vInfo.addAll( query.getResultList() );
        if (vInfo.isEmpty()) {
            final AssetTypeEntity typeEnt = entMgr.find(AssetTypeEntity.class, UUIDFactory.makeCleanString(oatype.getObjectId()));

            for (AssetTypeEntity subtype : typeEnt.getSubtypeList()) {
                vInfo.addAll( query.setParameter("typeId", subtype.getObjectId()).
                        getResultList()
                        );
                if (!vInfo.isEmpty()) {
                    break;
                }
            }
        }
        try {
            final Set<Asset> vResult = new HashSet<Asset>();
            for (AssetEntity ent : vInfo) {
                vResult.add(ent.buildAsset());
            }
            return vResult;
        } catch (AssetException ex) {
            throw new SQLException("Failed to resolve entity to asset", ex);
        }
    }
}
