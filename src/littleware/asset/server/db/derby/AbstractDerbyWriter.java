package littleware.asset.server.db.derby;

import littleware.db.DbSimpleWriter;


/**
* We currently require the application to manage 
 * Derby Connections.  This base class implements
 * saveObject( x_arg ) to throw UnsupportedOperationException -
 * clients must supply a JDBC Connection or DataSource for now.
 */
public abstract class AbstractDerbyWriter<T> extends DbSimpleWriter<T> {
    /** Constructor calls through to super */
	public AbstractDerbyWriter ( String s_query, boolean b_is_function ) {
        super ( s_query, b_is_function );
    }
    
	public void saveObject( T x_arg ) {
        throw new UnsupportedOperationException ();
    }
    
}


// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

