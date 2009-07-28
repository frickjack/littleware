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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.Query;
import javax.persistence.EntityManager;
import littleware.asset.IdWithClock;
import littleware.asset.IdWithClock.Builder;
import littleware.base.UUIDFactory;
import littleware.db.DbReader;

class DbLogLoader implements DbReader<List<IdWithClock>, Long> {

    private final Provider<JpaLittleTransaction> provideTrans;
    private final UUID homeId;
    private final IdWithClock.Builder clockBuilder;

    private DbLogLoader(Provider<JpaLittleTransaction> provideTrans,
            IdWithClock.Builder clockBuilder, UUID homeId ) {
        this.provideTrans = provideTrans;
        this.homeId = homeId;
        this.clockBuilder = clockBuilder;
    }

    /** Builder for clients to allocate things with */
    public static class Builder {
        private Provider<JpaLittleTransaction> provideTrans;
        private IdWithClock.Builder clockBuilder;
        @Inject
        public Builder( Provider<JpaLittleTransaction> provideTrans,
                IdWithClock.Builder clockBuilder ) {
            this.provideTrans = provideTrans;
            this.clockBuilder = clockBuilder;
        }

        public DbLogLoader build( UUID homeId ) {
            return new DbLogLoader( provideTrans, clockBuilder, homeId );
        }
    }

    @Override
    public List<IdWithClock> loadObject(Long minTransactionIn) throws SQLException {
        final EntityManager entMgr = provideTrans.get().getEntityManager();
        final TransactionEntity trans = entMgr.find( TransactionEntity.class, new Integer(1) );
        final String sQuery = "SELECT NEW littleware.asset.server.db.jpa.ClockIdType( x.objectId, x.lastTransaction ) " +
                "FROM Asset x " +
                "WHERE x.homeId = :homeId AND x.lastTransaction > :minTransaction " +
                "ORDER BY x.lastTransaction";
        final long minTransaction = (trans.getTransaction() - 200 < minTransactionIn.longValue()) ?
            (trans.getTransaction() - 200) : minTransactionIn.longValue();
        final Query query = entMgr.createQuery(sQuery).
                setParameter("homeId", UUIDFactory.makeCleanString(homeId)).
                setParameter("minTransaction", minTransaction );
        final List<ClockIdType> dataList = query.getResultList();
        final List<IdWithClock> result = new ArrayList<IdWithClock>();
        for( ClockIdType data : dataList ) {
            result.add( clockBuilder.build( UUIDFactory.parseUUID( data.getId() ),
                                            data.getTransaction()
                                            )
                                            );
        }
        return result;
    }
}
