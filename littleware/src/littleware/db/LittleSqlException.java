package littleware.db;


/**
 * Base class for exceptions thrown by classes in the littleware.db package.
 */
public class LittleSqlException extends java.sql.SQLException {
    /** Default constructor */
    public LittleSqlException () {
        super ( "Exception in littleware.base package" );
    }
    
    /** Constructor takes user-supplied exception message */
    public LittleSqlException ( String s_message ) {
        super ( s_message );
    }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

