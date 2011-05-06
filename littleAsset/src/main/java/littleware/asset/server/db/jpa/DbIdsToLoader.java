/*
 * Copyright 2011 http://code.google.com/p/littleware/
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.server.db.jpa;

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
    private final AssetType assetType;
    private final UUID toId;
    private final JpaLittleTransaction trans;

    public DbIdsToLoader( JpaLittleTransaction trans,
            UUID uTo, AssetType atype ) {
        assetType = atype;
        toId = uTo;
        this.trans = trans;
    }

    @Override
    public Set<UUID> loadObject(String sIgnore) throws SQLException {
        final EntityManager entMgr = trans.getEntityManager();
        final List<String>  vIds;
        if ( null != assetType ) {
            final AssetTypeEntity typeEnt = entMgr.find( AssetTypeEntity.class, UUIDFactory.makeCleanString( assetType.getObjectId() ) );
            final Query query = entMgr.createQuery( "SELECT x.objectId FROM Asset x WHERE x.toId=:toId AND x.typeId=:typeId").
                    setParameter( "toId", UUIDFactory.makeCleanString( toId ) ).
                    setParameter( "typeId", UUIDFactory.makeCleanString( assetType.getObjectId() ) );
            vIds = query.getResultList();
            for( AssetTypeEntity subtype : typeEnt.getSubtypeList() ) {
                vIds.addAll( query.setParameter( "typeId", subtype.getObjectId() ).getResultList() );
            }
        } else {
            vIds = entMgr.createQuery( "SELECT x.objectId FROM Asset x WHERE x.toId=:toId" ).
                    setParameter( "toId", UUIDFactory.makeCleanString( toId ) ).
                    getResultList();
        }
        final Set<UUID>     setResult = new HashSet<UUID> ();
        for( String sId : vIds ) {
            setResult.add( UUIDFactory.parseUUID(sId));
        }
        return setResult;
    }

}
