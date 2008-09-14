package littleware.base;

import java.util.UUID;

/**
 * Cacheable object subtypes have a globally unique
 * object id, and a monotionically increasing
 * transaction counter that takes on the value of a
 * globally incrementing counter after every update
 * to the object.  The object-id/transaction-counter
 * can be used by a cacheing system to decide whether
 * a local copy of an object is consistent with the
 * master copy, or to establish the Lamport-time relationship
 * between sets of objects cached on separate systems.
 */
public interface CacheableObject extends Comparable<CacheableObject>, Cloneable, java.io.Serializable {
	/** 
	 * Set the global id associated with the master object
	 * this object is a copy of.
	 */
	public void setObjectId ( UUID u_id );
	
	/**
	 * Get the object id
	 */
	public UUID getObjectId ();
	
	/**
	 * Reset the transaction counter
	 */
	public void setTransactionCount ( long l_transaction );
	
	/**
	 * Get the transaction count
	 */
	public long getTransactionCount ();
	
	/** Implementors must expose safe clone() call */
	public CacheableObject clone();
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

