package littleware.db;

import java.util.*;
import java.sql.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Just provides a utility for writing a list of
 * result objects to a database.
 * Avoid using this generic collection writer with a
 * DbWriter that issue subselects - you'll
 * be better off writing custom Collection-writer for
 * those types to avoid database load.
 */
public class DbCollectionWriter<T> extends DbSimpleWriter<Collection<? extends T>> {
	private static final Logger         ox_logger = Logger.getLogger ( "littleware.db.DbCollectionWriter" );
	private final JdbcDbWriter<T>           ox_writer;
	
	/**
	 * Just stash the base object-writer for writing each element of the collection.
	 */
	public DbCollectionWriter ( JdbcDbWriter<T> x_writer ) {
		super( "", false );
		ox_writer = x_writer;
	}
    
    /**
     * Just loop over single-object writer 
     */
    public void saveObject ( Collection<? extends T> v_data ) throws SQLException {
        for ( T x_data : v_data ) {
            ox_writer.saveObject ( x_data );
        }
    }
    
	
	/**
     * Just calls through to the prepareStatement(sql_conn,null) method
	 * of the object-writer supplied to this object's constructor.
	 *
	 * @param sql_conn connection to prepare the statement against
	 * @return statement possibly requiring parameters to be set against it -
	 *             caller must properly close() the statement
	 * @exception SQLException pass through exceptions thrown by sql_conn access
	 * @exception UnsupportedOperationException if not implemented by the
	 *                            underlying object
	 */
	public PreparedStatement prepareStatement ( Connection sql_conn ) throws SQLException
	{
		return ox_writer.prepareStatement ( sql_conn );
	}
	
	
	/**
	 * Save the specified collection by invoking x_writer.prepareSattment ( sql_conn, null ),
	 * then just invoking x_writer.saveObject for each element in v_collection.
	 *
	 * @param sql_stmt statement to execute a save operation against - obtain statement from
	 *                 x_writer.prepareStatement ( sql_conn, null )
	 * @param x_writer to write each member of the collection with
	 * @param v_collection to save
	 * @return result of the last call to sql_stmt.execute()
	 * @exception SQLException pass through exceptions thrown by sql_stmt access
	 */
	public static <T> boolean saveCollection ( PreparedStatement sql_stmt,
										   JdbcDbWriter<T> x_writer,
										   Collection<? extends T> v_collection
								) throws SQLException 
	{
		boolean           b_result = false;
		for ( Iterator<? extends T> r_i = v_collection.iterator ();
			  r_i.hasNext ();
			  ) {
			b_result = x_writer.saveObject ( sql_stmt, r_i.next () );
		}
		return b_result;
	}
	
	/**
	 * Save the specified object by setting parameters against and executing
	 * the supplied prepared statement.
	 *
	 * @param sql_stmt connection to execute a save operation against - possible obtained from
	 *                 this.prepareStatement ( sql_conn, x_object ),
	 *                 possibly null for some aggregate-based saves.
	 * @param v_collection to save
	 * @return result of last call to sql_stmt.execute()
	 * @exception SQLException pass through exceptions thrown by sql_stmt access
	 */
	public boolean saveObject ( PreparedStatement sql_stmt, Collection<? extends T> v_collection ) throws SQLException
	{
		return saveCollection ( sql_stmt, ox_writer, v_collection );
	}
}

		
	
	
// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

