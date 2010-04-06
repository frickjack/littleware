/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.tracker.server;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import littleware.apps.tracker.SimpleQueryBuilder;
import littleware.apps.tracker.TaskManager;
import littleware.apps.tracker.TaskQueryManager;
import littleware.apps.tracker.TaskQuery;
import littleware.apps.tracker.TaskSet;
import littleware.apps.tracker.TrackerAssetType;
import littleware.asset.server.NullAssetSpecializer;
import littleware.asset.server.db.jpa.JpaLittleTransaction;
import littleware.base.BaseException;
import littleware.base.UUIDFactory;

public class JpaTaskQueryManager implements TaskQueryManager {
    private static final Logger log = Logger.getLogger( JpaTaskQueryManager.class.getName() );
    private final Provider<JpaLittleTransaction> provideTrans;

    @Inject
    public JpaTaskQueryManager( Provider<JpaLittleTransaction> provideTrans ) {
        this.provideTrans = provideTrans;
    }

    @Override
    public Collection<UUID> runQuery(TaskQuery taskQuery ) throws BaseException, GeneralSecurityException, RemoteException {
        final SimpleQueryBuilder.Query tq = (SimpleQueryBuilder.Query) taskQuery;
        final JpaLittleTransaction trans = provideTrans.get();
        trans.startDbAccess();
        try {
            final EntityManager entMgr = trans.getEntityManager();
            final Query query = entMgr.createQuery( "SELECT x.objectId FROM Asset x WHERE x.toId=:toId AND x.typeId=:typeId").
                    setParameter( "toId", UUIDFactory.makeCleanString( tq.getQueueId() ) ).
                    setParameter( "typeId", UUIDFactory.makeCleanString( TrackerAssetType.TASK.getObjectId() ) );
            final List<String> ids = query.getResultList();
            return null;
        } finally {
            trans.endDbAccess();
        }
    }
}
