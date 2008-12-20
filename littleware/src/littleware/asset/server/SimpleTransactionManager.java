/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
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
import com.google.inject.Provider;

/**
 * TransactionManager implementation associated with
 * SimpleLittleTransaction JdbcLittleTransaction implementation.
 * This bypasses the AccessPermission on the littleware.db.SqlResourceBundle -
 * it's too much trouble to elevate our AccessControlContext on the
 * server whenever we need to access the db as part of the littleware.asset.Transaction refactor, 
 * and it's not necessary anyway.
 */
public class SimpleTransactionManager implements TransactionManager {
    private final Provider<LittleTransaction>     oprovide_trans;

    private final ThreadLocal<LittleTransaction>  othread_cache = new ThreadLocal<LittleTransaction>() {
        @Override
		protected LittleTransaction initialValue() {
			return oprovide_trans.get();
		}
	};
    
    //private static final TransactionManager om_trans = new SimpleTransactionManager ();
    
    @Inject
    public  SimpleTransactionManager ( Provider<LittleTransaction> provide_trans ) {
        oprovide_trans = provide_trans;
    }

    
    /**
     * Get the active Transaction associated with this thread.
     *
    public static TransactionManager getManager () {
        return om_trans;
    }
    */

    /**
     * NOTE: SimpleLittleTransaction.setDataSource must be called  before the
     *      first call here.
     */
    public LittleTransaction getThreadTransaction () {
        // this class implements both TransactionManager and LittleTransaction
        return othread_cache.get ();
    }
    
}

