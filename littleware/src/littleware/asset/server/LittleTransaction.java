/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.server;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import littleware.asset.Asset;

/**
 * Interface implemented by db-access managers -
 * provides methods to mark begin/end of db access
 * and updates.
 */
public interface LittleTransaction {

    /**
     * Inform the TransactionManager that you're beginning a database
     * access block.  Must call endDbAccess to close the block.
     * Must setup start/endTransaction block in addition or instead
     * of this around db-update blocks.
     *
     * @return TransactionManager map where client can check for assets that
     *          have already been looked up earlier in the transaction.
     */
    public Map<UUID,Asset> startDbAccess ();
    
    /** 
     * Recycle the cache and decrement the create-call count -
     * if the create-count reaches zero, then
     * clear the cache and close the cached Connection -
     * release any outstanding SavePoints.
     *
     * @param v_cache obtained by startDbAccess - used for sanity check if not null
     * @throws AssertionFailedException if v_cache does not belong to this factory,
     *                 or if recycle() is called more times than create() has been called
     *                 (create-count less than zero).
     */
    public void endDbAccess ( Map<UUID,Asset> v_cache );
    
    /**
     * Same as endDbAccess(map) without the sanity check.
     */
    public void endDbAccess ();
    
    /**
     * Let the cycle-cache know that a db-updating transaction is about to begin.
     * Sets up a SavePoint on the connection stashed with this TransactionManager.
     * Caller must notify this object of the end of the transaction by calling
     * endTransaction (below).  Does a startDbAccess() call that endTransaction matches
     * with a endDbAccess().
     *
     * @exception TransactionException if unable to setup transaction SavePoint
     */
    public void startDbUpdate () throws SQLException;
    
    /**
     * Return true if we are within a db-update transaction.
     * CacheManager keyes on this to disable cacheing when for a thread within
     * a transaction.
     */
    public boolean isDbUpdating ();
        
    /** Return the id associated with this transaction */
    public UUID getId ();
    
    
    /**
     * Notify the cycle cache of the end of the transaction,
     * and whether rollback is necessary.  Rolls back to
     * the last SavePoint (from startTransaction) or releases it.
     * If a rollback is necessary, then clear the contents of the
     * cycle-cache map, and discard the deferTillTransactionEnd commands
     * issued since the rollback destination Savepoint.
     * Also issues a recycle() call to match the create() done by startTransaction.
     * Finally, issues a commit() on the connection when the topmost transaction succeeds,
     * and issues all the deferTillTransactionEnd commands.
     *
     * @param b_rollback set true if the transaction failed, and needs rolled back
     *             to its SavePoint, otherwise release the SavePoint.
     * @exception NoSuchThingTransaction if a matching startTransaction does
     *             not exist.
     * @exception SQLException if db commit/rollback fails
     */
    public void endDbUpdate ( boolean b_rollback ) throws SQLException;
    
    /**
     * If isDbUpdating(), then defer the given Runnable action until
     * the transaction completely ends, otherwise
     * just run it now.
     */
    public void deferTillTransactionEnd ( Runnable run_later );
}
