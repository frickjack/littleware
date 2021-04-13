/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.db.memory;

import com.google.inject.Inject;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.persistence.EntityManager;
import littleware.asset.LittleHome;
import littleware.base.UUIDFactory;
import littleware.db.DbReader;

/**
 * Load home assets
 */
@SuppressWarnings("unchecked")
public class DbHomeLoader implements DbReader<Map<String, UUID>,String> {
    private final InMemLittleTransaction trans;

    @Inject
    public DbHomeLoader( InMemLittleTransaction trans ) {
        this.trans = trans;
    }

    @Override
    public Map<String, UUID> loadObject(String arg) throws SQLException {
        final EntityManager entMgr = trans.getEntityManager();
        final List<NameIdType> vInfo = entMgr.createQuery( "SELECT NEW littleware.asset.db.jpa.NameIdType( x.name, x.objectId, x.typeId ) FROM Asset x WHERE x.typeId='" +
                UUIDFactory.makeCleanString( LittleHome.HOME_TYPE.getObjectId() ) + "'"
                ).getResultList();
        final Map<String,UUID> mapResult = new HashMap<String,UUID>();

        for ( NameIdType info : vInfo ) {
            mapResult.put( info.getName(), info.getId() );
        }
        return mapResult;
    }
}
