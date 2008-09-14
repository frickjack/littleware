package littleware.db;

import java.sql.*;

/**
 * Interface standardizing pattern for extracting an object
 * from a database.
 * Implementation may or may not implement cacheing and logging.
 */
public interface DbReader<T,R> {
	/**
	 * Extract an object (type depends upon DataHandler implementation)
	 * from some underlying database.
	 *
	 * @param x_arg to parameterize the statement with before execution
	 * @return object extracted from db
	 * @exception SQLException pass through exceptions thrown by sql_rset access - 
     *                  should refactor this to something independent later - ugh
	 */
	public T loadObject( R x_arg ) throws SQLException;	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

