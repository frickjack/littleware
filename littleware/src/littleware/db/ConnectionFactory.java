package littleware.db;

import java.sql.Connection;
import javax.sql.DataSource;

import littleware.base.*;

/**
 * Little extention of littleware.base.Factory for defining
 * factories that manage an underlying SQL database connection
 * pool.  Most connection pool implementations require the 
 * user to recycle the connection once done with it.
 * Also implements the javax.sql.DataSource interface.
 * Connection objects obtained by this interface can use
 * the a ConnectionWrapper class that invokes 'recycle'
 * on connection-close to support the DataSource interface.
 */
public interface ConnectionFactory extends Factory<Connection>, DataSource {	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

