package littleware.asset.test;

import java.sql.Connection;
import java.sql.Savepoint;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.security.Principal;

import junit.framework.*;

import littleware.asset.*;
import littleware.asset.server.TransactionManager;
import littleware.asset.server.LittleTransaction;
import littleware.asset.server.JdbcTransaction;
import littleware.base.*;
import littleware.security.SecurityAssetType;


/**
 * Test littleware.asset.server.TransactionManager and supporting classes
 */
public class TransactionTester extends TestCase {
	private static final Logger olog_generic = Logger.getLogger ( "littleware.asset.test.TransactionTester" );
    
    /** No setup necessary */
    public void setUp () {}
    
    /** No tearDown necessary */
    public void tearDown () {}
    
    /**
     * Just call through to super
     */
    public TransactionTester ( String s_test_name ) {
        super ( s_test_name );
    }
    
    /**
     * Run TransactionManager through some basic tests
     */
    public void testTransactionManager () {
        try {
            LittleTransaction  trans_test = TransactionManager.getTheThreadTransaction ();
            Map<UUID,Asset>  v_cache = trans_test.startDbAccess ();
            assertTrue ( "TransactionManager maintains a singleton cache",
                         v_cache == trans_test.startDbAccess ()
                         );
            v_cache.put ( UUID.randomUUID (), null );
            trans_test.endDbAccess ( v_cache );
            assertTrue ( "First recycle did not clear cache map",
                         ! v_cache.isEmpty ()
                         );
            trans_test.endDbAccess ( v_cache );
            assertTrue ( "Closing recycle cleared the cache", v_cache.isEmpty () );
            try {
                trans_test.endDbAccess ( v_cache );
                assertTrue ( "Extra recycle should have thrown FactoryException", false );
            } catch ( Exception e ) {}
        } catch ( FactoryException e ) {
            assertTrue ( "Caught unexpected: " + e, false );
        }
    }
    
    
    /**
     * Test the JDBC Savepoint management.
     * Assumes that TransactionManager.getTheThreadTransaction returns a JdbcTranaction.
     */
    public void testSavepoint () {
        JdbcTransaction  trans_test = (JdbcTransaction) TransactionManager.getTheThreadTransaction ();        
        try {
            trans_test.startDbUpdate ();
            try {
                Connection conn_test = trans_test.getConnection ();
                Savepoint  savept_test = conn_test.setSavepoint ();
                conn_test.releaseSavepoint ( savept_test );
            } finally {
                trans_test.endDbUpdate ( true );                 
            }
        } catch ( SQLException e ) {
            olog_generic.log ( Level.WARNING, "Caught unexpected SQLException", e );
            assertTrue ( "Should not have cuaght: " + e, false );
        }
    }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

