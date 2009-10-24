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
import javax.sql.DataSource;
import java.util.logging.Logger;
import java.util.logging.Level;
import littleware.base.stat.Timer;

/**
 * Base class with some useful default methods for
 * implemntors of DbReader.
 */
public abstract class DbSimpleReader<T, R> implements JdbcDbReader<T, R> {

    private static final Logger olog_generic = Logger.getLogger("littleware.db.DbSimpleReader");
    private final String os_query;
    private final boolean ob_is_function;

    /**
     * Constructor stashes the query to prepare a statement with,
     * and whether the query should be prepared as a CallableStatement
     * that returns a refcursor, or as a generic SELECT.
     *
     * @param s_query to associate with this object
     * @param b_is_function set true if this query executes a SQL function, that should
     *              be setup as a JDBC CallableStatement
     * @param sql_factory default DataSource for this handler
     */
    public DbSimpleReader(String s_query, boolean b_is_function) {
        os_query = s_query;
        ob_is_function = b_is_function;
    }

    /** Subtypes override */
    @Override
    public abstract T loadObject(R x_arg) throws SQLException;

    @Override
    public PreparedStatement prepareStatement(Connection sql_conn) throws SQLException {
        if (ob_is_function) {
            CallableStatement sql_stmt = sql_conn.prepareCall(os_query);
            sql_stmt.registerOutParameter(1, Types.OTHER);
            return sql_stmt;
        } else {
            return sql_conn.prepareStatement(os_query);
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
        ResultSet sql_rset = null;
        final Timer timer = Timer.startTimer();

        try {
            if (sql_stmt instanceof CallableStatement) {
                sql_stmt.execute();
                sql_rset = (ResultSet) ((CallableStatement) sql_stmt).getObject(1);
            } else {
                sql_rset = sql_stmt.executeQuery();
            }
            olog_generic.log(Level.FINE, "Ran " + os_query + " in " + timer.sample() + " ms");
            return sql_rset;
        } catch (SQLException e) {
            olog_generic.log(Level.WARNING, "Running " + os_query + ", caught unexpected " + e);
            throw e;
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