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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import littleware.asset.AssetType;
import littleware.base.UUIDFactory;
import littleware.db.DbReader;

/**
 * IdsFrom database handler
 */
public class DbIdsFromLoader implements DbReader<Map<String, UUID>, String> {
    private static final Logger olog = Logger.getLogger( DbIdsFromLoader.class.getName() );
    private final AssetType oatype;
    private final UUID ouFrom;
    private final Provider<JpaLittleTransaction> oprovideTrans;

    public DbIdsFromLoader(Provider<JpaLittleTransaction> provideTrans,
            UUID uFrom, AssetType atype) {
        oatype = atype;
        ouFrom = uFrom;
        oprovideTrans = provideTrans;
    }

    @Override
    public Map<String, UUID> loadObject(String sIgnore) throws SQLException {
        final EntityManager entMgr = oprovideTrans.get().getEntityManager();
        final String sQuery;
        final List<NameIdType> vInfo;

        if ( null == oatype ) {
            sQuery = "SELECT NEW littleware.asset.server.db.jpa.NameIdType( x.name, x.objectId, x.typeId ) " +
                "FROM Asset x WHERE x.fromId=:fromId";

            vInfo = entMgr.createQuery(sQuery).
                setParameter("fromId", UUIDFactory.makeCleanString(ouFrom)).
                getResultList();
        } else {
            final AssetTypeEntity typeEnt = entMgr.find( AssetTypeEntity.class, UUIDFactory.makeCleanString( oatype.getObjectId() ) );
            sQuery = "SELECT NEW littleware.asset.server.db.jpa.NameIdType( x.name, x.objectId, x.typeId ) " +
                "FROM Asset x WHERE x.fromId=:fromId AND x.typeId=:typeId";
            final Query query = entMgr.createQuery(sQuery).
                setParameter("fromId", UUIDFactory.makeCleanString(ouFrom)).
                setParameter("typeId", UUIDFactory.makeCleanString(oatype.getObjectId()));
            vInfo = query.
                getResultList();
            for( AssetTypeEntity subtype : typeEnt.getSubtypeList() ) {
                vInfo.addAll( entMgr.createQuery(sQuery).
                    setParameter("fromId", UUIDFactory.makeCleanString(ouFrom)).
                    setParameter("typeId", subtype.getObjectId() ).
                    getResultList()
                    );
            }
        }
        olog.log( Level.FINE, "Ran " + sQuery + ", got: " + vInfo.size() );

        final Map<String, UUID> mapResult = new HashMap<String, UUID>();
        for (NameIdType info : vInfo) {
            if ( (null == oatype) || info.getType().isA(oatype)) {
                mapResult.put(info.getName(), info.getId());
            }
        }
        return mapResult;
    }
}
