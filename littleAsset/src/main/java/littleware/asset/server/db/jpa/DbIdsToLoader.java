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
public class DbIdsToLoader implements DbReader<Set<UUID>,String> {
    private final AssetType oatype;
    private final UUID ouTo;
    private final Provider<JpaLittleTransaction> oprovideTrans;

    public DbIdsToLoader( Provider<JpaLittleTransaction> provideTrans,
            UUID uTo, AssetType atype ) {
        oatype = atype;
        ouTo = uTo;
        oprovideTrans = provideTrans;
    }

    @Override
    public Set<UUID> loadObject(String sIgnore) throws SQLException {
        final EntityManager entMgr = oprovideTrans.get().getEntityManager();
        final List<String>  vIds;
        if ( null != oatype ) {
            final AssetTypeEntity typeEnt = entMgr.find( AssetTypeEntity.class, UUIDFactory.makeCleanString( oatype.getObjectId() ) );
            final Query query = entMgr.createQuery( "SELECT x.objectId FROM Asset x WHERE x.toId=:toId AND x.typeId=:typeId").
                    setParameter( "toId", UUIDFactory.makeCleanString( ouTo ) ).
                    setParameter( "typeId", UUIDFactory.makeCleanString( oatype.getObjectId() ) );
            vIds = query.getResultList();
            for( AssetTypeEntity subtype : typeEnt.getSubtypeList() ) {
                vIds.addAll( query.setParameter( "typeId", subtype.getObjectId() ).getResultList() );
            }
        } else {
            vIds = entMgr.createQuery( "SELECT x.objectId FROM Asset x WHERE x.toId=:toId" ).
                    setParameter( "toId", UUIDFactory.makeCleanString( ouTo ) ).
                    getResultList();
        }
        final Set<UUID>     setResult = new HashSet<UUID> ();
        for( String sId : vIds ) {
            setResult.add( UUIDFactory.parseUUID(sId));
        }
        return setResult;
    }

}
