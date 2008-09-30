package littleware.asset.server;


/**
 * Just an injector that allows a client to get
 * a reference to the LittleTransaction active on
 * the current thread stack.
 */
public abstract class TransactionManager {

    /**
     * Get the LittleTransaction active on the calling thread.
     */
    public abstract LittleTransaction getThreadTransaction ();
    
    /**
     * Entry/Configuration point for clients to get the active transaction manager.
     */
    public static TransactionManager getManager () {
        return SimpleTransactionManager.getManager ();
    }
    
    /**
     * Shortcut for getManager ().getThreadTransaction ()
     */
    public static LittleTransaction getTheThreadTransaction () {
        return getManager ().getThreadTransaction ();
    }
        
}


// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

