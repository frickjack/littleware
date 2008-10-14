package littleware.security.auth;

import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import javax.security.auth.Subject;
import java.security.GeneralSecurityException;

import littleware.asset.Asset;
import littleware.asset.AssetRetriever;
import littleware.asset.AssetException;
import littleware.base.DataAccessException;
import littleware.base.NoSuchThingException;
import littleware.base.BaseException;
import littleware.security.AccessDeniedException;


/**
 * Specialization of Asset for session-tracking.
 * The user the session is associated with is the session creator.
 */
public interface LittleSession extends Asset {
	/**
     * Is this a read-only user session (0 != getValue()) ?
	 */
	public boolean isReadOnly ();
	
    /**
     * Mark this session read-only (setValue(1)) - must save() this
	 * session asset for the change to take effect.
	 */
	public void makeReadOnly ();
	
	/**
	 * Mark this session read-write (setValue(0)) - must save() this
	 * session asset for the change to take effect.
	 */
	public void makeReadWrite ();
	
	/**
	 * Convenience method for getAsset ( getCreator () )...
	 */
	public Subject getSubject ( AssetRetriever m_retriever ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException;
	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

