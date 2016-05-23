/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.db;

import java.sql.*;

/**
 * Interface standardizing pattern for extracting an object
 * from a SQL ResultSet.
 * Idea is that different handlers may use each other.
 * For example - a handler that accesses a list of users from
 * a ResultSet may use a handler that extracts an individual user.
 * Implementation may or may not implement cacheing and logging.
 */
public interface JdbcDbReader<T, R> extends DbReader<T, R> {

    /**
     * Just a specialization of java.db.Connection.prepareStatement
     * to prepare a statement based on the implementation type and
     * the given object argument.
     *
     * @param sql_conn connection to prepare the statement against
     * @return statement possibly requiring parameters to be set against it -
     *             caller must properly close() the statement
     * @throws SQLException pass through exceptions thrown by sql_conn access
     * @throws LittleSqlException if unable to prepare statement
     * @throws UnsupportedOperationException if not implemented
     */
    public PreparedStatement prepareStatement(Connection sql_conn) throws SQLException;

    /**
     * Execute the given statement
     *
     * @param sql_stmt returned by prepareStatement
     * @param x_arg to parameterize the statement with before execution
     * @return ResultSet ready for extraction - properly handling ReferenceCursor
     *             results of CallableStatement - caller must close the ResultSet
     * @throws LittleSqlException if unable to prepare statement
     * @throws UnsupportedOperationException if not implemented
     */
    public ResultSet executeStatement(PreparedStatement sql_stmt, R x_arg) throws SQLException;

    /**
     * Extract an object (type depends upon DataHandler implementation)
     * from the db using a connection from the given data source.
     *
     * @param sql_data_source to connect with the database
     * @param x_arg to parameterize the statement with before execution
     * @return object extracted from db
     * @throws SQLException pass through exceptions thrown by sql_rset access
     */
    public T loadObject(javax.sql.DataSource sql_data_source, R x_arg) throws SQLException;

    /**
     * Extract an object (type depends upon DataHandler implementation)
     * from the db using the given connection.
     *
     * @param sql_conn to the database
     * @param x_arg to parameterize the statement with before execution
     * @return object extracted from db
     * @throws SQLException pass through exceptions thrown by sql_rset access
     */
    public T loadObject(Connection sql_conn, R x_arg) throws SQLException;

    /**
     * Extract an object (type depends upon DataHandler implementation)
     * from the result-set acquired by executing the given statement,
     * then close the result set.
     *
     * @param sql_stmt to execute
     * @param x_arg to parameterize the statement with before execution
     * @return object extracted from ResultSet, or null if x_rset.next()
     *        returns false
     * @throws SQLException pass through exceptions thrown by sql_rset access
     */
    public T loadObject(PreparedStatement sql_stmt, R x_arg) throws SQLException;

    /**
     * Extract an object (type depends upon DataHandler implementation)
     * from the given result-set.  Implicitly invoke x_rset.next ()
     * before performing extraction.
     *
     * @param sql_rset to extract from, possible from sql_stmt.executeQuery -
     *                caller must close the result set
     * @return object extracted from ResultSet, or null if x_rset.next()
     *        returns false
     * @throws SQLException pass through exceptions thrown by sql_rset access
     */
    public T loadObject(ResultSet sql_rset) throws SQLException;
}
