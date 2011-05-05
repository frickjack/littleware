/*
 * Copyright 2011 http://code.google.com/p/littleware
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.test;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.sql.Connection;
import java.sql.Savepoint;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;


import littleware.asset.*;
import littleware.asset.server.LittleTransaction;
import littleware.asset.server.JdbcTransaction;
import littleware.base.*;
import littleware.test.LittleTest;

/**
 * Test littleware.asset.server.TransactionManager and supporting classes
 */
public class TransactionTester extends LittleTest {

    private static final Logger log = Logger.getLogger( TransactionTester.class.getName() );
    private final Provider<LittleTransaction> transactionProvider;


    /**
     * Just call through to super
     */
    @Inject
    public TransactionTester(Provider<LittleTransaction> provideTrans) {
        setName("testTransactionManager");
        transactionProvider = provideTrans;
    }

    /**
     * Run TransactionManager through some basic tests
     */
    public void testTransactionManager() {
        try {
            final LittleTransaction trans_test = transactionProvider.get();
            final Map<UUID, Asset> v_cache = trans_test.startDbAccess();
            assertTrue("TransactionManager maintains a singleton cache",
                    v_cache == trans_test.startDbAccess());
            v_cache.put(UUID.randomUUID(), null);
            trans_test.endDbAccess(v_cache);
            assertTrue("First recycle did not clear cache map",
                    !v_cache.isEmpty());
            trans_test.endDbAccess(v_cache);
            assertTrue("Closing recycle cleared the cache", v_cache.isEmpty());
            try {
                trans_test.endDbAccess(v_cache);
                assertTrue("Extra recycle should have thrown FactoryException", false);
            } catch (Exception e) {
            }
        } catch (FactoryException e) {
            assertTrue("Caught unexpected: " + e, false);
        }
    }

    /**
     * Test the JDBC Savepoint management.
     * Assumes that TransactionManager.getTheThreadTransaction returns a JdbcTranaction.
     */
    public void testSavepoint() {
        JdbcTransaction trans_test = (JdbcTransaction) transactionProvider.get();
        try {
            trans_test.startDbUpdate();
            try {
                Connection conn_test = trans_test.getConnection();
                Savepoint savept_test = conn_test.setSavepoint();
                conn_test.releaseSavepoint(savept_test);
            } finally {
                trans_test.endDbUpdate(true);
            }
        } catch (SQLException e) {
            log.log(Level.WARNING, "Caught unexpected SQLException", e);
            assertTrue("Should not have cuaght: " + e, false);
        }
    }
}
