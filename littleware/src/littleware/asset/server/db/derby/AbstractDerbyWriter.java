package littleware.asset.server.db.derby;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import littleware.db.DbSimpleWriter;


/**
 * Stash the derby DataSource, and call through to super
 */
public abstract class AbstractDerbyWriter<T> extends DbSimpleWriter<T> {
    private final DataSource  odataSource;

    /** Constructor calls through to super */
	public AbstractDerbyWriter ( DataSource dataSource, String s_query, boolean b_is_function ) {
        super ( s_query, b_is_function );
        odataSource = dataSource;
    }
    
	public void saveObject( T x_arg ) throws SQLException {
        Connection conn = odataSource.getConnection ();
        try {
            saveObject( conn, x_arg );
        } finally {
            conn.close ();
        }
    }
    
}


// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

