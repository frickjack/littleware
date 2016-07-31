package littleware.db;

import java.sql.*;
import javax.sql.DataSource;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.javasimon.SimonManager;
import org.javasimon.Split;
import org.javasimon.Stopwatch;

/**
 * Base class with some useful default methods for
 * implemntors of DbReader.
 */
public abstract class DbSimpleReader<T, R> implements JdbcDbReader<T, R> {

    private static final Logger log = Logger.getLogger("littleware.db.DbSimpleReader");
    private final String query;
    private final boolean isFunction;
    private final Stopwatch stopWatch;

    /**
     * Constructor stashes the query to prepare a statement with,
     * and whether the query should be prepared as a CallableStatement
     * that returns a refcursor, or as a generic SELECT.
     *
     * @param query to associate with this object
     * @param isFunction set true if this query executes a SQL function, that should
     *              be setup as a JDBC CallableStatement
     * @param sql_factory default DataSource for this handler
     */
    public DbSimpleReader(String query, boolean isFunction) {
        this.query = query;
        this.isFunction = isFunction;
        this.stopWatch = SimonManager.getStopwatch( getClass().getName() );
    }

    /** Subtypes override */
    @Override
    public abstract T loadObject(R x_arg) throws SQLException;

    @Override
    public PreparedStatement prepareStatement(Connection sql_conn) throws SQLException {
        if (isFunction) {
            CallableStatement sql_stmt = sql_conn.prepareCall(query);
            sql_stmt.registerOutParameter(1, Types.OTHER);
            return sql_stmt;
        } else {
            return sql_conn.prepareStatement(query);
        }
    }

    /**
     * Implementation ignores argument - override for subtypes
     * that want to consider x_arg.
     *
     * @return ResultSet from execution of query or callable statement
     */
    @Override
    public ResultSet executeStatement(PreparedStatement sql_stmt, R x_arg) throws SQLException {
        try {
            final ResultSet rset; 
            final Split split = stopWatch.start();

            if (sql_stmt instanceof CallableStatement) {
                sql_stmt.execute();
                rset = (ResultSet) ((CallableStatement) sql_stmt).getObject(1);
            } else {
                rset = sql_stmt.executeQuery();
            }
            split.stop();
            if ( log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "Ran " + query + " in " + split + ", " + stopWatch );
            }
            return rset;
        } catch (SQLException ex) {
            log.log(Level.WARNING, "Running {0}, caught unexpected {1}", new Object[]{query, ex});
            throw ex;
        }
    }

    /*..
    public T loadObject( R x_arg ) throws SQLException {
    return loadObject ( osql_factory, x_arg );
    }
     */
    @Override
    public T loadObject(DataSource sql_data_source, R x_arg) throws SQLException {
        Connection sql_conn = null;
        try {
            sql_conn = sql_data_source.getConnection();
            return loadObject(sql_conn, x_arg);
        } finally {
            Janitor.cleanupSession(sql_conn);
        }
    }

    @Override
    public T loadObject(Connection sql_conn, R x_arg) throws SQLException {
        PreparedStatement sql_stmt = null;
        try {
            sql_stmt = prepareStatement(sql_conn);
            return loadObject(sql_stmt, x_arg);
        } finally {
            Janitor.cleanupSession(sql_stmt);
        }
    }

    @Override
    public T loadObject(PreparedStatement sql_stmt, R x_arg) throws SQLException {
        ResultSet sql_rset = null;
        try {
            sql_rset = executeStatement(sql_stmt, x_arg);
            return loadObject(sql_rset);
        } finally {
            Janitor.cleanupSession(sql_rset);
        }
    }

    @Override
    public abstract T loadObject(ResultSet sql_rset) throws SQLException;
}