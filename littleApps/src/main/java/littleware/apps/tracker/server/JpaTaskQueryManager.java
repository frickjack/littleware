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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import littleware.apps.tracker.Queue;
import littleware.apps.tracker.SimpleQueryBuilder;
import littleware.apps.tracker.SimpleTaskBuilder;
import littleware.apps.tracker.TaskManager;
import littleware.apps.tracker.TaskQueryManager;
import littleware.apps.tracker.TaskQuery;
import littleware.apps.tracker.TaskSet;
import littleware.apps.tracker.TaskStatus;
import littleware.apps.tracker.TrackerAssetType;
import littleware.asset.AssetSearchManager;
import littleware.asset.server.NullAssetSpecializer;
import littleware.asset.server.db.jpa.AssetEntity;
import littleware.asset.server.db.jpa.JpaLittleTransaction;
import littleware.base.BaseException;
import littleware.base.UUIDFactory;

public class JpaTaskQueryManager implements TaskQueryManager {
    private static final Logger log = Logger.getLogger( JpaTaskQueryManager.class.getName() );
    private final Provider<JpaLittleTransaction> provideTrans;
    private final AssetSearchManager search;

    @Inject
    public JpaTaskQueryManager( Provider<JpaLittleTransaction> provideTrans,
            AssetSearchManager search ) {
        this.provideTrans = provideTrans;
        this.search = search;
    }


    private static class QueryParameter {
        private final String key;
        private final Object value;
        public QueryParameter( String key, Object value ) {
            this.key = key;
            this.value = value;
        }
        public String getKey() { return key; }
        public Object getValue() { return value; }
    }

    @Override
    public Collection<UUID> runQuery(TaskQuery taskQuery ) throws BaseException, GeneralSecurityException, RemoteException {
        final SimpleQueryBuilder.Query tq = (SimpleQueryBuilder.Query) taskQuery;
        final JpaLittleTransaction trans = provideTrans.get();
        trans.startDbAccess();
        try {
            final List<QueryParameter> paramList = new ArrayList<QueryParameter>();
            final Queue         queue = search.getAsset( tq.getQueueId() ).get().narrow();
            final EntityManager entMgr = trans.getEntityManager();
            /*...
            final CriteriaBuilder criteriaBuilder = entMgr.getCriteriaBuilder();
            final CriteriaQuery<String>   query = criteriaBuilder.createQuery(String.class );
            final Root<AssetEntity>       root = query.from( AssetEntity.class );
            query.where( root.get( "homeId"))
             *
             */
            String queryPrefix = "SELECT x.objectId FROM Asset x, AssetLink link " +
                            "WHERE x.homeId=:homeId " +
                            "AND x.typeId='" + UUIDFactory.makeCleanString( TrackerAssetType.TASK.getObjectId() ) + "' " +
                            "AND link.asset.objectId=x.objectId AND link.key='" + SimpleTaskBuilder.QueueIdKey + "' " +
                            "AND link.value=:queueId ";
            paramList.add( new QueryParameter( "homeId", queue.getHomeId() ) );
            paramList.add( new QueryParameter( "queueId", queue.getId() ) );

            if ( tq.getTaskName().isSet() ) {
                queryPrefix += "AND x.name=:name ";
                paramList.add( new QueryParameter( "name", tq.getTaskName() ) );
            } else {
                if ( tq.getStatusMode().equals( SimpleQueryBuilder.Query.StatusMode.InState ) ) {
                    queryPrefix += "AND x.state=:state ";
                    paramList.add( new QueryParameter( "state", tq.getStatus().get() ) );
                } else if ( tq.getStatusMode().equals( SimpleQueryBuilder.Query.StatusMode.Active ) ) {
                    queryPrefix += "AND x.state > " + TaskStatus.MERGED.ordinal();
                } else if ( tq.getStatusMode().equals( SimpleQueryBuilder.Query.StatusMode.Finished ) ) { 
                    queryPrefix += "AND x.state <= " + TaskStatus.MERGED.ordinal();
                }
                
                if ( tq.getMinCreateDate().isSet() ) {
                    queryPrefix += "AND x.timeCreated >= :minCreateDate ";
                    paramList.add( new QueryParameter( "minCreateDate", tq.getMinCreateDate().get() ) );
                }
            }
            final Query   query = entMgr.createQuery( queryPrefix );
            final List<String> ids = query.getResultList();
            return null;
        } finally {
            trans.endDbAccess();
        }
    }
}
