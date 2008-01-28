package littleware.db;

import java.sql.*;
import java.util.logging.Logger;
import littleware.base.*;

/**
 * Janitor just exports a few utility methods for cleaning up
 * database sessions.  It's important that an application explicitly
 * close result-set and statement objects to avoid leaking cursors,
 * and recycle connections to maintain a connection pool.
 */
public abstract class Janitor {
    /**
     * Little utility that invokes close on the given parameters,
     * and captures and ignores exceptions.  Ignores null arguments.
     *
     * @param sql_rset to close or NULL for NOOP
     */
    public static void cleanupSession ( ResultSet sql_rset ) {
        try {
            if ( null != sql_rset ) {
                sql_rset.close ();
            }
        } catch ( SQLException e ) {}
    }
	
	/**
	 * Little utility that invokes close on the given parameters,
     * and captures and ignores exceptions.  Ignores null arguments.
     *
     * @param sql_stmt to close or NULL for NOOP
     */
    public static void cleanupSession ( Statement sql_stmt ) {
        try {
            if ( null != sql_stmt ) {
                sql_stmt.close ();
            }
        } catch ( SQLException e ) {}
    }
    
    /**
     * Little utility that invokes close on the given parameters,
     * and captures and ignores exceptions.  Ignores null arguments.
     * Closes sql_rset first, then sql_stmt.
     *
     * @param sql_rset to close
     * @param sql_stmt to close
     */
    public static void cleanupSession ( ResultSet sql_rset, java.sql.Statement sql_stmt ) {
        cleanupSession( sql_rset );
		cleanupSession( sql_stmt );
    }
    
    /**
     * Little utility that invokes close on the given parameters,
     * and captures and ignores exceptions.  Ignores null arguments.
     * Closes sql_rset first, then sql_stmt, then sql_conn.
     *
     * @param sql_rset to close
     * @param sql_stmt to close
     * @param sql_conn to close
     */
    public static void cleanupSession ( ResultSet sql_rset, Statement sql_stmt, Connection sql_conn ) {
        cleanupSession( sql_rset, sql_stmt );
		if ( null != sql_conn ) {
			 try {
			   sql_conn.commit ();
			} catch ( SQLException e ) {}
			try {
				sql_conn.close ();
			} catch ( SQLException e ) {}
		}
    }
	
	/**
	 * Little utility that invokes close on the given parameters,
     * and captures and ignores exceptions.  Ignores null arguments.
     * Closes sql_rset first, then sql_stmt, then sql_conn.
     *
     * @param sql_stmt to close
     * @param sql_conn to close
     */
    public static void cleanupSession ( Statement sql_stmt, Connection sql_conn ) {
        cleanupSession( null, sql_stmt, sql_conn );
    }
	
	/**
	 * Close the freakin' connection without throwin an exception.
	 */
	public static void cleanupSession( Connection sql_conn ) {
		if ( null != sql_conn ) {
			try {
				sql_conn.commit ();
			} catch ( SQLException e ) {}
			try {
				sql_conn.close ();
			} catch ( SQLException e ) {}
		}
	}
    

    /**
     * Little utility that invokes close on the given parameters,
     * and captures and ignores exceptions.  Ignores null arguments.
     * Closes sql_rset first, then sql_stmt, then recycles sql_conn into x_pool.
     *
     * @param sql_rset to close
     * @param sql_stmt to close
     * @param sql_conn to x_pool.recycle
     * @param x_pool to receive connection
     */
    public static void cleanupSession ( ResultSet sql_rset, Statement sql_stmt, Connection sql_conn, Factory x_pool ) {
        cleanupSession( sql_rset, sql_stmt );

        if ( (null != sql_conn) && (null != x_pool) ) {
			try {
				sql_conn.commit ();
			} catch ( SQLException e ) {}
            try {
                x_pool.recycle( sql_conn );
            } catch ( FactoryException e ) {
                Logger.getLogger ( "littleware.db" ).info ( "Caught unexpected exception recycling DB connection: " + e );
            }
        } else if ( null != sql_conn ) {
			cleanupSession ( sql_conn );
		}
    }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

