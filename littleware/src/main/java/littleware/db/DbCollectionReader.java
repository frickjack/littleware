package littleware.db;

import java.util.*;
import java.sql.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.base.BaseException;

/**
 * Just provides a utility for retrieving a list of
 * result objects from a result set.
 * Avoid using this generic collection reader with a
 * DbReader that issues subselects - you'll
 * be better off writing custom Collection-readers for
 * those types to avoid database load.
 */
public class DbCollectionReader<T,R> extends DbSimpleReader<Collection<T>,R> {
	private static final Logger           ox_logger = Logger.getLogger ( "littleware.db.DbCollectionReader" );
	private final JdbcDbReader<T,R>    ox_reader;
	
	/** 
	 * Constructor just stashes away a reference to a 
	 * reader that knows how to read individual members of a collection.
	 *
	 * @param x_reader to prepare statement against
	 * @throws InstantiationException if unable to invoke
	 *           v_base_collection.getClass().newInstance ()
	 * @throws IllegalAccessException if unable to invoke
	 *           v_base_collection.getClass().newInstance ()
	 */
	public DbCollectionReader ( JdbcDbReader<T,R> x_reader ) {
		super ( "collection reader", false );
		ox_reader = x_reader;
	}
    
    /**
     * Just throw UnsupportedOperationException - subtypes can override
     */
    public Collection<T> loadObject ( R x_arg ) {
        throw new UnsupportedOperationException ();
    }
	
	/**
	 * Just calls through to the internal reader's prepareStatement method.
	 *
	 * @param sql_conn connection to prepare the statement against
	 * @return statement possibly requiring parameters to be set against it -
	 *             caller must properly close() the statement
	 * @throws SQLException pass through exceptions thrown by sql_conn access
	 * @throws UnsupportedOperationException if not implemented
	 */
	public PreparedStatement prepareStatement ( Connection sql_conn ) throws SQLException	{
		return ox_reader.prepareStatement ( sql_conn );
	}


	/** Calls through  to reader's (see constructor) execute method */
	public ResultSet executeStatement( PreparedStatement sql_stmt, R x_arg ) throws SQLException {
		return ox_reader.executeStatement ( sql_stmt, x_arg );
	}
	
	
	

	
	/**
	 * Extract a collection from the given result-set using the reader
	 * supplied to the constructor to extract each object.  	 
	 *
	 * @param sql_rset to extract from, possible from sql_stmt.executeQuery -
	 *                caller must close the result set
	 * @return list of objects - possibly empty - extracted from db
	 * @throws SQLException pass through exceptions thrown by sql access
	 */
	public Collection<T> loadObject( ResultSet sql_rset ) throws SQLException {
		List<T> v_result = new Vector<T> ();
		for ( T x_result = ox_reader.loadObject ( sql_rset );
			  x_result != null;
			  x_result = ox_reader.loadObject ( sql_rset )
			  ) {
			v_result.add ( x_result );
		}
		return v_result;
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

