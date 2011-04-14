package littleware.db;

import java.sql.*;
import javax.sql.DataSource;

/**
 * Slight specialization of DbSimpleReader where
 * executeStatement does not ignore the argument -
 * rather passes it through via a setObject() call
 * on the PreparedStatement
 */
public abstract class DbSingleArgReader<T,R> extends DbSimpleReader<T,R> {

	/**
	 * Constructor just passes args through to DbSimpleReader ...
	 */
	public DbSingleArgReader ( String s_query, boolean b_function ) {
		super ( s_query, b_function );
	}
	
	
	/**
	 * Implementation sets argument sql_stmt.setObject ( 1, x_arg ),
	 * then calls through to super,executeStatement ( sql_stmt, null )
	 *
	 * @return ResultSet from execution of query or callable statement
	 */
	public ResultSet executeStatement( PreparedStatement sql_stmt, R x_arg ) throws SQLException {
		sql_stmt.setObject ( 1, x_arg );
		return super.executeStatement ( sql_stmt, null );
	}
	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

