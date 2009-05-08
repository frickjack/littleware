/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.server;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.Asset;
import littleware.base.AssertionFailedException;
import littleware.base.UUIDFactory;

/**
 * Abstract base-class for LittleTransaction implementations -
 * just manages the cycle cache, and provides utility methods
 * to help subtypes know the transaction state.
 */
public abstract class AbstractLittleTransaction implements LittleTransaction {
    private static final Logger    olog_generic = Logger.getLogger ( AbstractLittleTransaction.class.getName () );
    private final Map<UUID,Asset>  ov_cache = new HashMap<UUID,Asset> ();
    private final UUID             ou_id = UUIDFactory.getFactory ().create ();
    private int                    oi_count = 0;
    private Date                   ot_notzero = null;


    /** Return the id associated with this transaction */
    @Override
    public final UUID getId () { return ou_id; }


    @Override
    public Map<UUID,Asset> startDbAccess () {
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

    /** 
     * Subtypes hook in here - endDbAccess calls this
     *
     * @param iLevel
     */
    protected abstract void endDbAccess( int iLevel );

    /**
     * Implementation checks the cache, decrements the level counter,
     * then call endDbAccess( iLevel )
     *
     * @param v_cache provide user check to verify that we haven't lost track
     *         of the cycle-cache that goes with this transaction, may be null
     *         to skip check
     */
    @Override
    public final void endDbAccess ( Map<UUID,Asset> v_cache ) {
        if ( (null != v_cache)
             && (v_cache != ov_cache)
             ) {
            throw new AssertionFailedException ( "endDbAccess with wrong cache object" );
        }
        if ( 0 == oi_count ) {
            throw new IllegalStateException ( "start/endDbAccess mismatch" );
        }
        --oi_count;
        if ( (0 == oi_count) && (! ostack_savept.isEmpty ()) ) {
            throw new IllegalStateException ( "Non-empty savepoint stack at dbaccess end" );
        }
        if ( 0 == oi_count ) {
            ov_cache.clear();
        }
        endDbAccess( oi_count );
    }


    @Override
    public final void endDbAccess () {
        endDbAccess ( null );
    }



    // Little flag to set when running deferred actions for sanity check
    private boolean                    ob_running_deferred = false;
    private final List<Runnable>             ov_deferred_actions = new ArrayList<Runnable> ();

    private class MySavePoint {
        private final int oiLevel = oi_count;
        private final int oiDeferSize = ov_deferred_actions.size();

        public int getLevel() { return oiLevel; }
        public int getDeferSize() { return oiDeferSize; }
    }
    private final List<MySavePoint>       ostack_savept = new ArrayList<MySavePoint>();

    @Override
    public final void deferTillTransactionEnd ( Runnable run_later ) {
        if ( isDbUpdating () ) {
            ov_deferred_actions.add ( run_later );
        } else if ( ob_running_deferred ) {
            throw new IllegalStateException ( "Deferred actions may not defer actions" );
        } else {
            run_later.run ();
        }
    }


    @Override
    public void startDbUpdate () throws SQLException {
        startDbAccess ();
        ostack_savept.add( new MySavePoint() );
    }

    @Override
    public final boolean isDbUpdating () {
        return (! ostack_savept.isEmpty() );
    }

    /** Subtypes hook into endDbUpdate here */
    protected abstract void endDbUpdate( boolean b_rollback, int iUpdateLevel ) throws SQLException;

    /**
     * Performs sanity check before calling endDbUpdate with the
     * post-decremented level number.  Executes deferred actions
     * after subtype endDbUpdate( b_rollback, iUpdateLevel) runs.
     *
     * @param b_rollback
     * @throws java.sql.SQLException
     */
    @Override
    public final void endDbUpdate ( final boolean b_rollback ) throws SQLException {
        if ( ! isDbUpdating() ) {
            throw new IllegalStateException( "Not updating" );
        }
        final MySavePoint  savept = ostack_savept.remove( ostack_savept.size() - 1 );

        if ( oi_count != savept.getLevel () ) {
            throw new IllegalStateException ( "Transaction " + getId () +
                                              " level mismatch: " + oi_count +
                                              " != " + savept.getLevel ()
                                              );
        }

        if (b_rollback) {
            olog_generic.log(Level.FINE, "Clearing cycle cache before rollback");
            if (0 == savept.getDeferSize()) {
                ov_deferred_actions.clear();
            } else {
                for (int i = ov_deferred_actions.size() - 1;
                        i >= savept.getDeferSize(); --i) {
                    ov_deferred_actions.remove(i);
                }
            }
            ov_cache.clear();
        }

        // Upcall to subtype
        try {
            endDbUpdate( b_rollback, ostack_savept.size() );

            if ( ostack_savept.isEmpty() && (! b_rollback) ) {
                ob_running_deferred = true;
                for (Runnable run_now : ov_deferred_actions) {
                    try {
                        run_now.run();
                    } catch (Exception e) {
                        olog_generic.log(Level.SEVERE, "Failed deferred action", e);
                    }
                }
                ob_running_deferred = false;
                ov_deferred_actions.clear();
            }
        } finally {
            if ( ostack_savept.isEmpty () ) {
                ov_deferred_actions.clear ();
            }

            endDbAccess ();
        }
    }
}
