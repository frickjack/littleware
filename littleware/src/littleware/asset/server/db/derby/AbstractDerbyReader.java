package littleware.asset.server.db.derby;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import littleware.db.DbSimpleReader;


/**
 * Stash the derby DataSource, and call through to super
 */
public abstract class AbstractDerbyReader<T,R> extends DbSimpleReader<T,R> {
    private final DataSource  odataSource;

    /** Constructor calls through to super */
	protected AbstractDerbyReader ( DataSource dataSource, String s_query, boolean b_is_function ) {
        super ( s_query, b_is_function );
        odataSource = dataSource;
    }
    

    /**
     * Checkout/close the SQL Connection, and call through to
     * loadObject( conn, x_arg )
     */
	public T loadObject( R x_arg ) throws SQLException {
        Connection conn = odataSource.getConnection ();
        try {
            return loadObject( conn, x_arg );
        } finally {
            conn.close ();
        }
    }

}


// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

