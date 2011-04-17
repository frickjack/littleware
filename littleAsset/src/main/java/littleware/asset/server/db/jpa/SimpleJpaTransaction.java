/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.db.jpa;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import littleware.asset.Asset;
import littleware.asset.server.AbstractLittleTransaction;

/**
 * JPA supported implementation of LittleTransaction stuff
 */
public class SimpleJpaTransaction extends AbstractLittleTransaction implements JpaLittleTransaction {

    private static final Logger log = Logger.getLogger(SimpleJpaTransaction.class.getName());
    private final Provider<EntityManager> oprovideEntMgr;
    private EntityManager oentMgr = null;

    @Inject
    SimpleJpaTransaction(Provider<EntityManager> provideEntMgr) {
        oprovideEntMgr = provideEntMgr;
    }
    private int oiLevel = 0;

    @Override
    public Map<UUID, Asset> startDbAccess() {
        ++oiLevel;
        return super.startDbAccess();
    }

    @Override
    public EntityManager getEntityManager() {
        if (oiLevel < 1) {
            throw new IllegalStateException("Must setup transaction block before accessing getEntityManager");
        }
        if (null == oentMgr) {
            oentMgr = oprovideEntMgr.get();
        }
        return oentMgr;
    }

    @Override
    protected void endDbAccess(int iLevel) {
        if ((0 == iLevel) && (null != oentMgr)) {
            oentMgr.close();
            oentMgr = null;
        }
    }
    private long transactionCounter = -1L;

    @Override
    public long getTimestamp() {
        if (!isDbUpdating()) {
            throw new IllegalStateException("Update-transaction not initialized");
        }
        if (transactionCounter < 0) {
            throw new IllegalStateException("Internal error - invalid transaction counter: " + transactionCounter);
        }
        return transactionCounter;
    }

    @Override
    public void startDbUpdate() {
        if (!isDbUpdating()) {
            if (null == oentMgr) {
                oentMgr = oprovideEntMgr.get();
            }
            oentMgr.getTransaction().begin();
            final TransactionEntity trans = oentMgr.find(TransactionEntity.class, new Integer(1));
            transactionCounter = trans.getTimestamp() + 1;
            trans.setTimestamp(transactionCounter);
        }
        super.startDbUpdate();
    }

    @Override
    protected void endDbUpdate(boolean b_rollback, int iUpdateLevel) {
        if (0 == iUpdateLevel) {
            try {
                if ( oentMgr.getTransaction().getRollbackOnly() ) {
                    oentMgr.getTransaction().rollback();
                    if ( ! b_rollback ) {
                        throw new IllegalStateException( "Attempt to commit rollback-only transaction" );
                    }
                } else if (b_rollback) {
                    oentMgr.getTransaction().rollback();
                } else {
                    oentMgr.getTransaction().commit();
                }
            } finally {
                transactionCounter = -1L;
            }
            log.log(Level.FINE, "Transaction complete, rollback: {0}", b_rollback);
        } else if (b_rollback) {
            oentMgr.getTransaction().setRollbackOnly();
            throw new IllegalStateException("Nested rollback not supported by this transaction implementation");
        }
    }
}
