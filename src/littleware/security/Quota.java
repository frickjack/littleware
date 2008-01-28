package littleware.security;

import java.rmi.RemoteException;
import java.util.UUID;
import java.security.GeneralSecurityException;

import littleware.asset.Asset;
import littleware.asset.AssetRetriever;
import littleware.asset.AssetException;
import littleware.base.*;



/**
 * Quota asset gets attached to a user to restrict
 * the user's access to the littleware database in some way.
 */
public interface Quota extends Asset {

	/**
	 * Get the op-count associated with the given quota
	 */
	public int getQuotaCount ();
	
	/**
	 * Set the op-count associated with the given quota
	 */
	public void setQuotaCount ( int i_value );
	
	/**
	 * Get the op-count limit associated with the given quota
	 */
	public int getQuotaLimit ();
	
	/**
	 * Set the op-count limit associated with the given quota
	 */
	public void setQuotaLimit ( int i_value );
	
	
	/**
	 * Shortcut for setQuotaCount ( getQuotaCount () + 1 )
	 */
	public void incrementQuotaCount ();
	
	
	
	/** Shortcut for getFromId() */
	public UUID getUserId ();
	
	/** Shortcut for getToId () */
	public UUID getNextInChainId ();
	
	/**
	 * Get the next quota in the quota-chain.
	 * Shortcut for getTo()
	 */
	public Quota getNextInChain ( AssetRetriever m_retriever 
								  ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException;
	
	/**
	 * Get the user this quota is associated with (note - may be associated
     * with other users via a quota-chain).  Shortcut for getFrom()
	 */
	public LittleUser getUser ( AssetRetriever m_retriever ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException;
								
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

