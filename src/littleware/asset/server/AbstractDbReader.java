package littleware.asset.server;

import java.sql.SQLException;

import littleware.db.DbSimpleReader;

/**
 * Specialization of DbSimpleReader that pulls
 * its db connection from TransactionManager.getConnection.
 */
public abstract class AbstractDbReader<T,R> extends DbSimpleReader<T,R> {
    /** Constructor calls through to super */
	public AbstractDbReader ( String s_query, boolean b_is_function ) {
        super ( s_query, b_is_function );
    }
    
	
    public T loadObject( R x_arg ) throws SQLException {
        JdbcTransaction  trans_me = (JdbcTransaction) TransactionManager.getTheThreadTransaction ();
        trans_me.startDbAccess ();
        try {
            return loadObject ( trans_me.getConnection (),
                                x_arg
                                );
        } finally {
            trans_me.endDbAccess ();
        }
    }

}


// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

