package littleware.db;

import java.sql.*;

/**
 * Interface standardizing pattern for saving an object
 * to a SQL database.
 * Idea is that different writers may leverage each other under the hood -
 * so can write a writer that saves a list of objects that uses
 * a writer that saves an individual object. 
 */
public interface JdbcDbWriter<T> extends DbWriter<T> {
	/**
     * Just a specialization of java.sql.Connection.prepareStatement
	 * to prepare a statement based on the implementation type and the
	 * given object argument.
	 *
	 * @param sql_conn connection to prepare the statement against
	 * @return statement possibly requiring parameters to be set against it -
	 *             caller must properly close() the statement
	 * @exception SQLException pass through exceptions thrown by sql_conn access
	 * @exception UnsupportedOperationException if not implemented for complex aggregate-object saves
	 */
	public PreparedStatement prepareStatement ( Connection sql_conn ) throws SQLException;
	
	
	/**
     * Save the specified object by setting parameters against and executing
	 * the supplied prepared statement.
	 *
	 * @param sql_stmt connection to execute a save operation against - possible obtained from
	 *                 this.prepareStatement ( sql_conn, x_object ),
	 *                 possibly null for some aggregate-based saves.
	 * @param x_object to save
	 * @return result of sql_stmt.execute()
	 * @exception SQLException pass through exceptions thrown by sql_stmt access
	 */
	public boolean saveObject ( PreparedStatement sql_stmt, T x_object ) throws SQLException;
	
	/**
     * Save an object using the specified connection
	 *
	 * @param sql_conn to issue commands against
	 * @param x_object to save
	 * @exception SQLException
	 */
	public void saveObject ( Connection sql_conn, T x_object ) throws SQLException;
	
	/**
     * Save an object - extract and close a connection from the given DataSource
	 *
	 * @param sql_data_source to get connection from
	 * @param x_object to save
	 * @exception SQLException on SQL interaction failures
	 */
	public void saveObject ( javax.sql.DataSource sql_data_source, T x_object ) throws SQLException;
		
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

