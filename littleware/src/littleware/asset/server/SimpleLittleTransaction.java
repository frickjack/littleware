/*
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.server;

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
import littleware.base.AssertionFailedException;
import littleware.base.UUIDFactory;


/** 
 * Utility class maintains a ThreadLocal
 * Asset cache intended so that different functions
 * invoked by a thread to process a single request
 * can avoid retrieving the same Asset multiple times.
 * Each call to create() increments an internal count for that thread,
 * and each call to recycle() decrements the count -
 * recycle() clears the cache if the count is zero after
 * decrement.  AssertionFailedException gets thrown if endDbAccess() decrements below zero.
 * Also provides a place to stash a db connection so that
 * multiple db ops over a thread stack may share the same transaction.
 */
public class SimpleLittleTransaction implements JdbcTransaction {
    private static final Logger    olog_generic = Logger.getLogger ( SimpleLittleTransaction.class.getName () );


    private final DataSource      odatasource;
    
    
    private final Map<UUID,Asset>  ov_cache = new HashMap<UUID,Asset> ();
    private final UUID             ou_id = UUIDFactory.getFactory ().create ();
    private int                    oi_count = 0;
    private Date                   ot_notzero = null;
    private Connection             oconn_db = null;
    
    /**
     * Constructor for client that wants to setup his own cache
     * rather than share the per-thread cache.
     * Use getThreadTransaction to share the thread cache.
     *
     * @exception IllegalStateException if data-source not set
     */
    @Inject
    public SimpleLittleTransaction ( @Named( "datasource.littleware" ) DataSource datasource ) {
        odatasource = datasource;
    }
    

    /** Return the id associated with this transaction */
    @Override
    public UUID getId () { return ou_id; }
    
    /**
     * Need to call this before getConnection will work.
     * Static injector - initialization code needs to setup the SimpleLittleTransaction
     * with the DataSource by which it can setup Db connections for a 
     * thread's transaction.  This class is messy, but at least the mess is
     * here rather than spread all over.  Also moving to interfaces
     * that will support a move to JPA later.
     *
     * @param dsource_db global data source to allocate connections with
     *
    public static void setDataSource ( DataSource dsource_db ) {
        mdsource_db = dsource_db;
    }
      */

    @Override
    public synchronized Map<UUID,Asset> startDbAccess () {
        Date t_now = new Date ();
        
        if ( (oi_count > 0) 
             && (t_now.getTime () > ot_notzero.getTime () + 300000)
             ) {
            olog_generic.log ( Level.WARNING, "Cyclecache not zeroed for over 5 minutes - probably missing a recycle() call" );
        } else if ( oi_count == 0 ) {
            ot_notzero = new Date ();
        }
        ++oi_count;
        /*..
        olog_generic.log ( Level.FINE, "Transaction " + getId () + " checkout count: " + 
                           oi_count + ", size: " + ov_cache.size () // + ", " + Whatever.getStackTrace ()
                           );
        ..*/
        return ov_cache;
    }
    
        

    @Override
    public synchronized void endDbAccess ( Map<UUID,Asset> v_cache ) {
        if ( (null != v_cache)
             && (v_cache != ov_cache)
             ) {
            throw new AssertionFailedException ( "endDbAccess with wrong cache object" );
        }
        if ( 0 == oi_count ) {
            throw new IllegalStateException ( "start/endDbAccess mismatch" );
        }
        --oi_count;
        if ( 0 == oi_count ) {
            olog_generic.log ( Level.FINE, "Clearing cycle cache" );
            ov_cache.clear ();
            if ( null != oconn_db ) { // close connection
                olog_generic.log ( Level.FINE, "Closing cached connection" );

                if ( ! ostack_savept.isEmpty () ) {
                    throw new IllegalStateException ( "Non-empty savepoint stack at dbaccess end" );
                }
                try {
                    oconn_db.commit ();
                } catch ( SQLException e ) {
                    olog_generic.log ( Level.WARNING, "connection commit caught unexpected", e );
                }                 
                try {
                    oconn_db.close ();
                } catch ( SQLException e ) {
                    olog_generic.log ( Level.WARNING, "connection close caught unexpected", e );
                } 
                oconn_db = null; 
            }
        }
        //olog_generic.log ( Level.FINE, "Transaction " + getId () + " endAccess count: " + oi_count );
    }
    

    @Override
    public void endDbAccess () {
        endDbAccess ( null );
    }

    

    @Override
    public synchronized Connection getConnection () throws SQLException {
        if ( oi_count < 1 ) {
            throw new SQLException ( "Must setup transaction block before accessing SimpleLittleTransaction.getConnection" );
        }
        if ( null == oconn_db ) {
            // sanity check - having Savepiont issues 
            if ( ! ostack_savept.isEmpty () ) {
                throw new IllegalStateException ( "No savepoints should be present at this point" );
            }
            oconn_db = odatasource.getConnection ();
            oconn_db.setAutoCommit ( false );
        }
        return oconn_db;
    }
    
    
    /**
     * Little extended Savepoint that includes both a JDBC savepoint
     * and a position into the deferTillTransactionEnd command list
     */
    private class MySavepoint {
        private Savepoint  osavept_jdbc = null; 
        private final int        oi_command_size = ov_deferred_actions.size ();
        private final int        oi_level = oi_count;
        
        /**
         * Automatically stashes a getConnection.setSavepoint (),
         * and records the current length of the deferTillTransactionEnd list.
         *
         * @exception SQLException on failure to get JDBC savepoint
         */
        public MySavepoint () throws SQLException {
            osavept_jdbc = getConnection ().setSavepoint ();
        }
        
        /**
         * Release the savepoint when no longer needed
         */
        public void      release () {
            // try NOOP here - see what happens
            try {
                getConnection ().releaseSavepoint ( osavept_jdbc );
            } catch ( SQLException e ) {
                // savepoint gone ?
                olog_generic.log ( Level.WARNING, "Savepoint release caught unexpected", e );
            }
            osavept_jdbc = null;
        }
        
        /**
         * Get the transaction level current when this MySavepoint was created
         */
        public int getLevel () {
            return oi_level;
        }

        /** Rollback to this savepoint */
        public void      rollback () throws SQLException {
            olog_generic.log ( Level.FINE, "Clearing cycle cache before rollback" );

            if ( null == osavept_jdbc ) {
                throw new NullPointerException ();
            }
            if ( 0 == oi_command_size ) {
                ov_deferred_actions.clear ();
            } else if ( ov_deferred_actions.size () > oi_command_size ) {
                ov_deferred_actions = ov_deferred_actions.subList ( 0, oi_command_size );
            }
            ov_cache.clear ();
            getConnection ().rollback ( osavept_jdbc );
            /*..
            } catch ( SQLException e ) {
                olog_generic.log ( Level.WARNING, "Lost savepoint on rollback" , e );
                if ( ostack_savept.isEmpty () ) {
                    // just rollback all - this is a hack until we figure out what's going
                    // on with Savepoints
                    getConnection ().rollback ();
                }
            }
                ..*/    
            // - client must call release! - getConnection ().releaseSavepoint ( osavept_jdbc );
        }
    }
        
    private LinkedList<MySavepoint>    ostack_savept = new LinkedList<MySavepoint> ();
    private List<Runnable>             ov_deferred_actions = new ArrayList<Runnable> ();
    
    @Override
    public synchronized void startDbUpdate () throws SQLException {
        startDbAccess ();
        ostack_savept.add ( new MySavepoint () );
    }
    
    @Override
    public synchronized boolean isDbUpdating () {
        return (! ostack_savept.isEmpty ());
    }
    
    
    @Override
    public synchronized void endDbUpdate ( boolean b_rollback ) throws SQLException {
        MySavepoint  savept_trans = ostack_savept.removeLast ();
        
        if ( oi_count != savept_trans.getLevel () ) {
            throw new IllegalStateException ( "Transaction " + getId () + 
                                              " level mismatch: " + oi_count +
                                              " != " + savept_trans.getLevel ()
                                              );
        }
        try {
            if ( b_rollback ) {
                savept_trans.rollback ();
                savept_trans.release ();
            } else {
                savept_trans.release ();
                if ( ostack_savept.isEmpty () ) {
                    getConnection ().commit ();
               
                    ob_running_deferred = true;
                    for ( Runnable run_now : ov_deferred_actions ) {
                        try {
                            run_now.run ();
                        } catch ( Exception e ) {
                            olog_generic.log ( Level.SEVERE, "Failed deferred action", e );
                        }
                    }
                    ob_running_deferred = false;
                    ov_deferred_actions.clear ();
                }
            }
        } finally {
            if ( ostack_savept.isEmpty () ) {
                ov_deferred_actions.clear ();
            }
                
            endDbAccess ();
        }
    }

    // Little flag to set when running deferred actions for sanity check
    private boolean ob_running_deferred = false;
    
    
    @Override
    public void deferTillTransactionEnd ( Runnable run_later ) {
        if ( isDbUpdating () ) {
            ov_deferred_actions.add ( run_later );
        } else if ( ob_running_deferred ) {
            throw new IllegalStateException ( "Deferred actions may not defer actions" );
        } else {
            run_later.run ();
        }
    }
}

