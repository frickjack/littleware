/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.server.db;

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
import littleware.asset.server.LittleTransaction;
import littleware.base.AssertionFailedException;
import littleware.base.UUIDFactory;

/**
 * Abstract base-class for LittleTransaction implementations -
 * just manages the cycle cache, and provides utility methods
 * to help subtypes know the transaction state.
 */
public abstract class AbstractLittleTransaction implements LittleTransaction {
    private static final Logger    log = Logger.getLogger ( AbstractLittleTransaction.class.getName () );
    private final Map<UUID,Asset>  assetCache = new HashMap<> ();
    private final UUID             transactionId = UUIDFactory.getFactory ().get ();
    private int                    callCounter = 0;
    private Date                   transactionTimer = null;


    /** Return the id associated with this transaction */
    @Override
    public final UUID getId () { return transactionId; }


    @Override
    public Map<UUID,Asset> startDbAccess () {
        final Date t_now = new Date ();

        if ( (callCounter > 0)
             && (t_now.getTime () > transactionTimer.getTime () + 300000)
             ) {
            try {
                throw new AssertionFailedException();
            } catch ( Exception ex ) {
                log.log ( Level.WARNING, "Cyclecache not zeroed for over 5 minutes - probably missing a recycle() call", ex );
            }
            // do not vomit log messages - reset timer back 100 seconds
            transactionTimer = new Date ( new Date().getTime() - 200000 );
        } else if ( callCounter == 0 ) {
            transactionTimer = new Date ();
        }
        ++callCounter;
        /*..
        olog_generic.log ( Level.FINE, "Transaction " + getId () + " checkout count: " +
                           oi_count + ", size: " + ov_cache.size () // + ", " + Whatever.getStackTrace ()
                           );
        ..*/
        return assetCache;
    }

    @Override
    public int getNestingLevel() {
        return callCounter;
    }

    @Override
    public Map<UUID,Asset> getCache() {
        return assetCache;
    }

    /** 
     * Subtypes hook in here - endDbAccess calls this
     *
     * @param levelNumber
     */
    protected abstract void endDbAccess( int levelNumber );

    /**
     * Implementation checks the cache, decrements the level counter,
     * then call endDbAccess( iLevel )
     *
     * @param transCache provide user check to verify that we haven't lost track
     *         of the cycle-cache that goes with this transaction, may be null
     *         to skip check
     */
    @Override
    public final void endDbAccess ( Map<UUID,Asset> transCache ) {
        if ( (null != transCache)
             && (transCache != assetCache)
             ) {
            throw new AssertionFailedException ( "endDbAccess with wrong cache object" );
        }
        if ( 0 == callCounter ) {
            throw new IllegalStateException ( "start/endDbAccess mismatch" );
        }
        --callCounter;
        if ( (0 == callCounter) && (! savePointStack.isEmpty ()) ) {
            throw new IllegalStateException ( "Non-empty savepoint stack at dbaccess end" );
        }
        if ( 0 == callCounter ) {
            assetCache.clear();
        }
        endDbAccess( callCounter );
    }


    @Override
    public final void endDbAccess () {
        endDbAccess ( null );
    }



    // Little flag to set when running deferred actions for sanity check
    private boolean                    runningDeffered = false;
    private final List<Runnable>             deferredActions = new ArrayList<> ();

    private class MySavePoint {
        private final int updateLevel = callCounter;
        private final int deferSize = deferredActions.size();

        public int getLevel() { return updateLevel; }
        public int getDeferSize() { return deferSize; }
    }
    private final List<MySavePoint>       savePointStack = new ArrayList<>();

    @Override
    public final void deferTillTransactionEnd ( Runnable runLater ) {
        if ( isDbUpdating () ) {
            deferredActions.add ( runLater );
        } else if ( runningDeffered ) {
            throw new IllegalStateException ( "Deferred actions may not defer actions" );
        } else {
            runLater.run ();
        }
    }


    @Override
    public void startDbUpdate () {
        startDbAccess ();
        savePointStack.add( new MySavePoint() );
    }

    @Override
    public final boolean isDbUpdating () {
        return (! savePointStack.isEmpty() );
    }

    /** Subtypes hook into endDbUpdate here */
    protected abstract void endDbUpdate( boolean rollback, int updateLevel );

    /**
     * Performs sanity check before calling endDbUpdate with the
     * post-decremented level number.  Executes deferred actions
     * after subtype endDbUpdate( b_rollback, iUpdateLevel) runs.
     *
     * @param rollback
     * @throws java.sql.SQLException
     */
    @Override
    public final void endDbUpdate ( final boolean rollback ) {
        if ( ! isDbUpdating() ) {
            throw new IllegalStateException( "Not updating" );
        }
        final MySavePoint  savept = savePointStack.remove( savePointStack.size() - 1 );

        if ( callCounter != savept.getLevel () ) {
            throw new IllegalStateException ( "Transaction " + getId () +
                                              " level mismatch: " + callCounter +
                                              " != " + savept.getLevel ()
                                              );
        }

        if (rollback) {
            log.log(Level.FINE, "Clearing cycle cache before rollback");
            if (0 == savept.getDeferSize()) {
                deferredActions.clear();
            } else {
                for (int i = deferredActions.size() - 1;
                        i >= savept.getDeferSize(); --i) {
                    deferredActions.remove(i);
                }
            }
            assetCache.clear();
        }

        // Upcall to subtype
        try {
            endDbUpdate( rollback, savePointStack.size() );

            if ( savePointStack.isEmpty() && (! rollback) ) {
                runningDeffered = true;
                for (Runnable run_now : deferredActions) {
                    try {
                        run_now.run();
                    } catch (Exception e) {
                        log.log(Level.SEVERE, "Failed deferred action", e);
                    }
                }
                runningDeffered = false;
                deferredActions.clear();
            }
        } finally {
            if ( savePointStack.isEmpty () ) {
                deferredActions.clear ();
            }

            endDbAccess ();
        }
    }

    private final Map<String,Object> dataMap = new HashMap<>();

    @Override
    public void putData( String key, Object value ) {
        dataMap.put( key, value );
    }

    @Override
    public Object getData( String key ) {
        return dataMap.get( key );
    }
}
