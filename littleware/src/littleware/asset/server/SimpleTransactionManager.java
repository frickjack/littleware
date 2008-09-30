package littleware.asset.server;

/**
 * TransactionManager implementation associated with
 * SimpleLittleTransaction JdbcLittleTransaction implementation.
 * This bypasses the AccessPermission on the littleware.db.SqlResourceBundle -
 * it's too much trouble to elevate our AccessControlContext on the
 * server whenever we need to access the db as part of the littleware.asset.Transaction refactor, 
 * and it's not necessary anyway.
 */
public class SimpleTransactionManager extends TransactionManager {
    private static final ThreadLocal<SimpleLittleTransaction>  othread_cache = new ThreadLocal<SimpleLittleTransaction>() {
		protected SimpleLittleTransaction initialValue() {
			return new SimpleLittleTransaction ();
		}
	};
    
    private static final TransactionManager om_trans = new SimpleTransactionManager ();
    
    /** Hidden constructor */
    private SimpleTransactionManager () {}
    
    /**
     * Get the active Transaction associated with this thread.
     */
    public static TransactionManager getManager () {
        return om_trans;
    }

    /**
     * NOTE: SimpleLittleTransaction.setDataSource must be called  before the
     *      first call here.
     */
    public LittleTransaction getThreadTransaction () {
        // this class implements both TransactionManager and LittleTransaction
        SimpleLittleTransaction cache_thread = othread_cache.get ();
        return cache_thread;
    }
    
}


// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

