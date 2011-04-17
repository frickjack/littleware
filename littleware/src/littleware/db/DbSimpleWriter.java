package littleware.db;

import java.sql.*;
import javax.sql.DataSource;
import java.security.*;


/**
 * Simple partial implementation of DbWriter - 
 * provides partial default implementation of interface.
 */
public abstract class DbSimpleWriter<T> implements JdbcDbWriter<T> {
	private final String      os_query;
	private final boolean     ob_is_function;
	//private final DataSource  osql_factory;
	
	
	/**
	 * Constructor stashes the query to prepare a statement with,
	 * and whether the query should be prepared as a CallableStatement
	 * that returns void, or a prepared statement.
	 *
	 * @param s_query to associate with this object
	 * @param b_is_function set true if this query executes a SQL function, that should
	 *              be setup as a JDBC CallableStatement
	 */
	public DbSimpleWriter ( String s_query, boolean b_is_function ) {
        os_query = s_query;
        ob_is_function = b_is_function;
	}
	

    /**
     * Subtypes should override depending on how the implementation
     * accesses the database.
     */
    public abstract void saveObject ( T x_object ) throws SQLException;

	
	/**
	 * Just a specialization of java.sql.Connection.prepareStatement
	 * to prepare a statement based on the implementation type and the
	 * given object argument.
	 *
	 * @param sql_conn connection to prepare the statement against
	 * @return statement possibly requiring parameters to be set against it -
	 *             caller must properly close() the statement
	 * @throws SQLException pass through exceptions thrown by sql_conn access
	 * @throws UnsupportedOperationException if not implemented for complex aggregate-object saves
	 */
	public PreparedStatement prepareStatement ( Connection sql_conn ) throws SQLException {
		if ( ob_is_function ) {
			CallableStatement sql_stmt = sql_conn.prepareCall ( os_query );
			return sql_stmt;
		} else {
			return sql_conn.prepareStatement ( os_query );
		}
	}
	
	
	
	/**
	 * Save the specified object by setting parameters against and executing
	 * the supplied prepared statement.
	 *
	 * @param sql_stmt connection to execute a save operation against - possible obtained from
	 *                 this.prepareStatement ( sql_conn, x_object ),
	 *                 possibly null for some aggregate-based saves.
	 * @param x_object to save
	 * @return result of sql_stmt.execute()
	 * @throws SQLException pass through exceptions thrown by sql_stmt access
	 */
	public abstract boolean saveObject ( PreparedStatement sql_stmt, T x_object ) throws SQLException;
	
	/**
	 * Save an object using the specified connection.
	 *
	 * @param sql_conn to issue commands against
	 * @param x_object to save
	 * @throws SQLException
	 */
	public void saveObject ( Connection sql_conn, T x_object ) throws SQLException {
		PreparedStatement sql_stmt = null;
		try {
			sql_stmt = this.prepareStatement ( sql_conn );
			saveObject ( sql_stmt, x_object );
		} finally {
			Janitor.cleanupSession ( sql_stmt );
		}
	}
	
	/**
	 * Save an object - extract and close a connection from the given DataSource
	 *
	 * @param sql_data_source to get connection from
	 * @param x_object to save
	 * @throws SQLException on SQL interaction failures
	 */
	public void saveObject ( javax.sql.DataSource sql_data_source, T x_object ) throws SQLException
	{
		Connection sql_conn = null;
		try {
			sql_conn = sql_data_source.getConnection ();
			saveObject ( sql_conn, x_object );
		} finally {
			Janitor.cleanupSession ( sql_conn );
		}
	}
	
    /*..
	public void saveObject ( T x_object ) throws SQLException {
		saveObject ( osql_factory, x_object );
	}
    ..*/

}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

