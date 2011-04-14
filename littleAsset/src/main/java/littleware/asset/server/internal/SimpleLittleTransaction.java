/*
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.internal;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.sql.Connection;
import java.sql.Savepoint;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.sql.DataSource;

import littleware.asset.Asset;
import littleware.asset.server.AbstractLittleTransaction;
import littleware.asset.server.JdbcTransaction;
import littleware.base.AssertionFailedException;

/** 
 * Specialization of AbstractLittleTransaction mixes in JdbcLittleTransaction
 * provides a place to stash a db connection so that
 * multiple db ops over a thread stack may share the same transaction.
 */
public class SimpleLittleTransaction extends AbstractLittleTransaction
        implements JdbcTransaction {

    private static final Logger olog_generic = Logger.getLogger(SimpleLittleTransaction.class.getName());
    private final DataSource odatasource;
    private Connection oconn_db = null;

    /**
     * Constructor for client that wants to setup his own cache
     * rather than share the per-thread cache.
     * Use getThreadTransaction to share the thread cache.
     *
     * @exception IllegalStateException if data-source not set
     */
    @Inject
    public SimpleLittleTransaction(@Named("datasource.littleware") DataSource datasource) {
        odatasource = datasource;
    }

    private int   oiLevel = 0;

    @Override
    public Map<UUID,Asset> startDbAccess() {
        ++oiLevel;
        return super.startDbAccess();
    }

    @Override
    protected void endDbAccess( int iLevel) {
        oiLevel = iLevel;
        if ((0 == iLevel) && (null != oconn_db)) { // close connection
            olog_generic.log(Level.FINE, "Closing cached connection");

            if (!ostack_savept.isEmpty()) {
                throw new IllegalStateException("Non-empty savepoint stack at dbaccess end");
            }
            try {
                oconn_db.commit();
            } catch (SQLException e) {
                olog_generic.log(Level.WARNING, "connection commit caught unexpected", e);
            }
            try {
                oconn_db.close();
            } catch (SQLException e) {
                olog_generic.log(Level.WARNING, "connection close caught unexpected", e);
            }
            oconn_db = null;
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (oiLevel < 1) {
            throw new SQLException("Must setup transaction block before accessing SimpleLittleTransaction.getConnection");
        }

        if (null == oconn_db) {
            // sanity check - having Savepiont issues 
            if (!ostack_savept.isEmpty()) {
                throw new IllegalStateException("No savepoints should be present at this point");
            }

            oconn_db = odatasource.getConnection();
            oconn_db.setAutoCommit(false);
        }

        return oconn_db;
    }

    @Override
    public long getTransaction() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Little extended Savepoint that includes both a JDBC savepoint
     * and a position into the deferTillTransactionEnd command list
     */
    private class MySavepoint {
        private Savepoint osavept_jdbc = null;


        /**
         * Automatically stashes a getConnection.setSavepoint (),
         * and records the current length of the deferTillTransactionEnd list.
         *
         * @exception SQLException on failure to get JDBC savepoint
         */
        public MySavepoint() throws SQLException {
            osavept_jdbc = getConnection().setSavepoint();
        }

        /**
         * Release the savepoint when no longer needed
         */
        public void release() {
            // try NOOP here - see what happens
            try {
                getConnection().releaseSavepoint(osavept_jdbc);
            } catch (SQLException e) {
                // savepoint gone ?
                olog_generic.log(Level.WARNING, "Savepoint release caught unexpected", e);
            }
            osavept_jdbc = null;
        }


        /** Rollback to this savepoint */
        public void rollback() throws SQLException {
            olog_generic.log(Level.FINE, "Clearing cycle cache before rollback");

            if (null == osavept_jdbc) {
                throw new NullPointerException();
            }

            getConnection().rollback(osavept_jdbc);
        }
    }
    private LinkedList<MySavepoint> ostack_savept = new LinkedList<MySavepoint>();
    private List<Runnable> ov_deferred_actions = new ArrayList<Runnable>();

    @Override
    public synchronized void startDbUpdate() {
        startDbAccess();
        try {
            ostack_savept.add(new MySavepoint());
        } catch (SQLException ex) {
            throw new IllegalStateException( "Failed to setup transaction save point", ex );
        }
    }


    @Override
    public void endDbUpdate(boolean b_rollback, int iLevel ) {
        final MySavepoint savept_trans = ostack_savept.removeLast();
        if ( iLevel != ostack_savept.size() ) {
            throw new AssertionFailedException( "Savepoint stack out of sync with transaction block" );
        }

        try {
            if (b_rollback) {
                savept_trans.rollback();
                savept_trans.release();
            } else {
                savept_trans.release();
                if ( 0 == iLevel ) {
                    getConnection().commit();
                }
            }
        } catch ( SQLException ex ) {
            throw new IllegalStateException( "Failed commit/rollback", ex );
        } finally {
            if (ostack_savept.isEmpty()) {
                ov_deferred_actions.clear();
            }

            endDbAccess();
        }

    }
}

