package littleware.asset.server;

import java.sql.Connection;
import java.sql.SQLException;


/**
 * Specialization of LittleTransaction - adds
 * getConnection() method to support JDBC based
 * persistance.  May add other subtypes later
 * for JPA, whatever.
 */
public interface JdbcTransaction extends LittleTransaction {
    /**
     * Return the db connection stashed with this cycle cache.
     * Pull connection from data source if none yet cached.
     * May only be called within a startDbX/endDbX block,
     * so we can make sure to return the Connection
     * to the underlying data source once this thread's processing completes.
     *
     * @exception SQLException if unable to access data source, or if not
     *               within create/recycle block
     */
    public Connection getConnection () throws SQLException;
        
}
