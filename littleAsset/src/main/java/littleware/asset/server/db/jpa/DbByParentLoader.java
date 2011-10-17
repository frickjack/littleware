/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.db.jpa;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import littleware.asset.Asset;
import littleware.asset.AssetType;
import littleware.asset.spi.AssetProviderRegistry;
import littleware.base.BaseException;
import littleware.base.Maybe;
import littleware.base.Option;
import littleware.base.UUIDFactory;
import littleware.db.DbReader;

/**
 * Load name-unique assets with the given name
 */
class DbByParentLoader implements DbReader<Option<Asset>, String> {
    private static final Logger log = Logger.getLogger( DbByParentLoader.class.getName() );

    private final String name;
    private final UUID parentId;
    private final JpaLittleTransaction trans;
    private final AssetProviderRegistry assetRegistry;

    public DbByParentLoader( JpaLittleTransaction trans,
            AssetProviderRegistry assetRegistry,
            String name, UUID parentId ) {
        this.name = name;
        this.parentId = parentId;
        this.trans = trans;
        this.assetRegistry = assetRegistry;
    }

    @Override
    public Option<Asset> loadObject(String sIgnore) throws SQLException {
        final EntityManager entMgr = trans.getEntityManager();
        final String queryString = "SELECT x FROM Asset x WHERE x.fromId=:fromId AND x.name=:name";
        final List<AssetEntity> info = new ArrayList<AssetEntity>();
        final Query query = entMgr.createQuery(queryString).
                setParameter("name", name).
                setParameter("fromId", UUIDFactory.makeCleanString( parentId ) );
        info.addAll(query.getResultList());
        
        try {
            final Set<Asset> result = new HashSet<Asset>();
            for (AssetEntity ent : info) {
                final AssetType assetType = AssetType.getMember(UUIDFactory.parseUUID(ent.getTypeId()));
                log.log( Level.FINE, "Building asset from entity with type: {0}", assetType);
                return Maybe.something(ent.buildAsset(assetRegistry.getService(assetType).get()));
            }
            return Maybe.empty();
        } catch (BaseException ex) {
            throw new SQLException("Failed to resolve entity to asset", ex);
        }
    }
}
