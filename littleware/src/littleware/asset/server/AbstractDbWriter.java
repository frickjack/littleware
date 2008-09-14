package littleware.asset.server;

import java.sql.SQLException;

import littleware.db.DbSimpleWriter;

/**
 * Specialization of DbSimpleWriter that pulls
 * its db connection from TransactionManager.getConnection.
 */
public abstract class AbstractDbWriter<T> extends DbSimpleWriter<T> {
    /** Constructor calls through to super */
	public AbstractDbWriter ( String s_query, boolean b_is_function ) {
        super ( s_query, b_is_function );
    }
    
	public void saveObject( T x_arg ) throws SQLException {
        JdbcTransaction    ltrans_me = (JdbcTransaction) TransactionManager.getTheThreadTransaction ();
        boolean            b_rollback = true;
        
        ltrans_me.startDbUpdate ();
        try {
            saveObject ( ltrans_me.getConnection (),
                                x_arg
                                );
            b_rollback = false;
        } finally {
            ltrans_me.endDbUpdate ( b_rollback );
        }
    }
    
}


// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

