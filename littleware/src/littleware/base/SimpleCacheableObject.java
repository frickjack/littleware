/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.base;

import java.util.UUID;


/**
 * Simple base implementation of CacheableObject
 */
public abstract class SimpleCacheableObject implements CacheableObject {
	private   UUID   ou_id = null;
	private   long   ol_transaction = -1;
	
	public SimpleCacheableObject () {}
	
	/**
	 * Initialize id and transaction count
	 */
	public SimpleCacheableObject ( UUID u_id, long l_transaction ) {
		ou_id = u_id;
		ol_transaction = l_transaction;
	}
	
	/** 
	 * Set the global id associated with the master object
	 * this object is a copy of.
	 */
    @Override
	public void setObjectId ( UUID u_id ) { ou_id = u_id; }
	
	/**
	 * Get the object id
	 */
    @Override
	public UUID getObjectId () { return ou_id; }
	
	/**
	 * Reset the transaction counter
	 */
    @Override
	public void setTransactionCount ( long l_transaction ) {
		ol_transaction = l_transaction;
	}
	
	/**
	 * Get the transaction count
	 */
    @Override
	public long getTransactionCount () { return ol_transaction; }
	
	/**
	 * Comparable interface
	 */
    @Override
	public int compareTo ( CacheableObject x_other ) {
		if ( (null == ou_id) || (null == x_other) || (null == x_other.getObjectId ()) ) {
			throw new NullPointerException ( "Cannot compare CacheAbleObjects without a valid ObjectId" );
		}
		return ou_id.compareTo ( x_other.getObjectId () );
	}
	
    @Override
	public boolean equals ( Object x_other ) {
		if ( (null == ou_id) || (null == x_other) 
			 || (! (x_other instanceof CacheableObject))
			 || (null == ((CacheableObject) x_other).getObjectId ()) 
			 ) {
			return false;
		}
		return ou_id.equals ( ((CacheableObject) x_other).getObjectId () );
	}
		
	
	/**
	 * Just return this.getObjectId ().hashCode ()
	 */
    @Override
	public int hashCode () { return this.getObjectId ().hashCode (); }
	
	/**
	 * Return a simple copy of this object
	 */
    @Override
	public CacheableObject clone () {
		try {
			return (CacheableObject) super.clone ();
		} catch ( CloneNotSupportedException e ) {
			throw new AssertionFailedException ( "What the frick with clone?", e );
		}
	}
	
}

