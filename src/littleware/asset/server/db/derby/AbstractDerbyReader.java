package littleware.asset.server.db.derby;

import littleware.db.DbSimpleReader;


/**
 * We currently require the application to manage 
 * Derby Connections.  This base class implements
 * loadObject( x_arg ) to throw UnsupportedOperationException -
 * clients must supply a JDBC Connection or DataSource for now.
 */
public abstract class AbstractDerbyReader<T,R> extends DbSimpleReader<T,R> {
    /** Constructor calls through to super */
	public AbstractDerbyReader ( String s_query, boolean b_is_function ) {
        super ( s_query, b_is_function );
    }
    
    
	public T loadObject( R x_arg ) {
        throw new UnsupportedOperationException ();
    }

}


// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

