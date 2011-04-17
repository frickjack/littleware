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

import com.google.inject.Inject;
import com.google.inject.Provider;
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
 *
 * @author pasquini
 */
public class DbHomeLoader implements DbReader<Map<String, UUID>,String> {
    private final Provider<JpaLittleTransaction> oprovideTrans;

    @Inject
    public DbHomeLoader( Provider<JpaLittleTransaction> provideTrans ) {
        oprovideTrans = provideTrans;
    }

    @Override
    public Map<String, UUID> loadObject(String x_arg) throws SQLException {
        final EntityManager entMgr = oprovideTrans.get().getEntityManager();
        final List<NameIdType> vInfo = entMgr.createQuery( "SELECT NEW littleware.asset.server.db.jpa.NameIdType( x.name, x.objectId, x.typeId ) FROM Asset x WHERE x.typeId='" +
                UUIDFactory.makeCleanString( LittleHome.HOME_TYPE.getObjectId() ) + "'"
                ).getResultList();
        final Map<String,UUID> mapResult = new HashMap<String,UUID>();

        for ( NameIdType info : vInfo ) {
            mapResult.put( info.getName(), info.getId() );
        }
        return mapResult;
    }
}
