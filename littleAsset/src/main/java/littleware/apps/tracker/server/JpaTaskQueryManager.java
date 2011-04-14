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

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import littleware.apps.tracker.Queue;
import littleware.apps.tracker.SimpleQueryBuilder;
import littleware.apps.tracker.SimpleTaskBuilder;
import littleware.apps.tracker.TaskQueryManager;
import littleware.apps.tracker.TaskQuery;
import littleware.apps.tracker.TaskStatus;
import littleware.apps.tracker.TrackerAssetType;
import littleware.asset.AssetSearchManager;
import littleware.asset.server.db.jpa.JpaLittleTransaction;
import littleware.base.BaseException;
import littleware.base.UUIDFactory;
import littleware.base.validate.ValidationException;


@Singleton
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
            final StringBuilder queryPrefix = new StringBuilder(
                    "SELECT x.objectId FROM Asset x JOIN x.linkSet link "
                    ).append( "WHERE x.homeId=:homeId "
                    ).append( "AND x.typeId='"
                    ).append( UUIDFactory.makeCleanString( TrackerAssetType.TASK.getObjectId() )
                    ).append( "' "
                    ).append( "AND link.key='"
                    ).append( SimpleTaskBuilder.QueueIdKey
                    ).append( "' "
                    ).append( "AND link.value=:queueId " );
            paramList.add( new QueryParameter( "homeId", 
                    UUIDFactory.makeCleanString( queue.getHomeId() )
                    ) );
            paramList.add( new QueryParameter( "queueId", 
                    UUIDFactory.makeCleanString( queue.getId() )
                    ) );

            if ( tq.getTaskName().isSet() ) {
                queryPrefix.append( "AND x.name=:name " );
                paramList.add( new QueryParameter( "name", tq.getTaskName().get() ) );
            } else {
                // Try to specify narrow constraints first
                if ( tq.getStatusMode().equals( SimpleQueryBuilder.Query.StatusMode.InState ) ) {
                    queryPrefix.append( "AND x.state=:state " );
                    paramList.add( new QueryParameter( "state", tq.getStatus().get() ) );
                } else if ( tq.getStatusMode().equals( SimpleQueryBuilder.Query.StatusMode.Active ) ) {
                    queryPrefix.append( "AND x.state > "
                            ).append( TaskStatus.MERGED.ordinal() ).append( " " );
                } else if ( tq.getStatusMode().equals( SimpleQueryBuilder.Query.StatusMode.Finished ) ) {
                    queryPrefix.append( "AND x.state <= "
                            ).append( TaskStatus.MERGED.ordinal() ).append( " " );
                }
                
                if ( tq.getMinCreateDate().isSet() ) {
                    queryPrefix.append( "AND x.timeCreated >= :minCreateDate " );
                    paramList.add( new QueryParameter( "minCreateDate", tq.getMinCreateDate().get() ) );
                    if ( tq.getMaxCreateDate().isSet()
                            && tq.getMaxCreateDate().get().getTime() < tq.getMinCreateDate().get().getTime()
                            ) {
                        throw new ValidationException( "Query max-create date less than min-create date" );
                    }
                }
                if ( tq.getMaxCreateDate().isSet() ) {
                    queryPrefix.append( "AND x.timeCreated <= :maxCreateDate " );
                    paramList.add( new QueryParameter( "maxCreateDate", tq.getMaxCreateDate().get() ) );
                }
                if ( tq.getMinModifyDate().isSet() ) {
                    queryPrefix.append( "AND x.timeUpdated >= :minUpdateDate " );
                    paramList.add( new QueryParameter( "minUpdateDate", tq.getMinModifyDate().get() ) );
                    if ( tq.getMaxModifyDate().isSet()
                            && tq.getMaxModifyDate().get().getTime() < tq.getMinModifyDate().get().getTime()
                            ) {
                        throw new ValidationException( "Query max-modify date less than min-modify date" );
                    }
                }
                if ( tq.getMaxModifyDate().isSet() ) {
                    queryPrefix.append( "AND x.timeUpdated <= :maxUpdateDate " );
                    paramList.add( new QueryParameter( "maxUpdateDate", tq.getMaxModifyDate().get() ) );
                }
                if ( tq.getSubmittedBy().isSet() ) {
                    queryPrefix.append( "AND x.creatorId=:creatorId " );
                    paramList.add( new QueryParameter( "creatorId", 
                            UUIDFactory.makeCleanString(tq.getSubmittedBy().get() )
                            ) );
                }
                if ( tq.getAssignedTo().isSet() ) {
                    queryPrefix.append( "AND x.toId=:toId " );
                    paramList.add( new QueryParameter( "toId",
                            UUIDFactory.makeCleanString( tq.getAssignedTo().get() )
                            ) );
                }
                queryPrefix.append( "ORDER BY x.timeUpdated DESC" );
            }
            final Query   query = entMgr.createQuery( queryPrefix.toString() );
            for( QueryParameter param : paramList ) {
                query.setParameter(param.getKey(), param.getValue() );
            }
            final List<String> ids = query.getResultList();
            final ImmutableList.Builder<UUID> builder = ImmutableList.builder();
            for( String id : ids ) {
                builder.add( UUIDFactory.parseUUID(id));
            }
            return builder.build();
        } finally {
            trans.endDbAccess();
        }
    }
}
