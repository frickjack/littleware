package littleware.asset.server.db.jpa;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.Query;
import javax.persistence.EntityManager;
import littleware.asset.IdWithClock;
import littleware.base.UUIDFactory;
import littleware.db.DbReader;

@SuppressWarnings("unchecked")
class DbLogLoader implements DbReader<List<IdWithClock>, Long> {

    private final UUID homeId;
    private final JpaLittleTransaction trans;
    private final IdWithClock.Builder clockBuilder;

    public DbLogLoader( JpaLittleTransaction trans,
            IdWithClock.Builder clockBuilder, UUID homeId ) {
        this.homeId = homeId;
        this.trans = trans;
        this.clockBuilder = clockBuilder;
    }


    @Override
    public List<IdWithClock> loadObject(Long minTransactionIn) throws SQLException {
        final EntityManager entMgr = trans.getEntityManager();
        final TransactionEntity entity = entMgr.find( TransactionEntity.class, 1 );
        final String sQuery = "SELECT NEW littleware.asset.server.db.jpa.ClockIdType( x.objectId, x.fromId, x.lastTransaction ) " +
                "FROM Asset x " +
                "WHERE x.homeId = :homeId AND x.lastTransaction > :minTransaction " +
                "ORDER BY x.lastTransaction";
        final long minTransaction = (entity.getTimestamp() - 200 > minTransactionIn.longValue()) ?
            (entity.getTimestamp() - 200) : minTransactionIn.longValue();
        final Query query = entMgr.createQuery(sQuery).
                setParameter("homeId", UUIDFactory.makeCleanString(homeId)).
                setParameter("minTransaction", minTransaction );
        final List<ClockIdType> dataList = query.getResultList();
        final List<IdWithClock> result = new ArrayList<IdWithClock>();
        for( ClockIdType data : dataList ) {
            result.add( clockBuilder.build( UUIDFactory.parseUUID( data.getId() ),
                                            UUIDFactory.parseUUID( data.getFromId() ),
                                            data.getTimestamp()
                                            )
                                            );
        }
        return result;
    }
}
