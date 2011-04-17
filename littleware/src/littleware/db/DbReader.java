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
	 * @throws SQLException pass through exceptions thrown by sql_rset access - 
     *                  should refactor this to something independent later - ugh
	 */
	public T loadObject( R x_arg ) throws SQLException;	
}

