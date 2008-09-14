package littleware.security.auth;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;
import java.security.GeneralSecurityException;
import java.net.URL;

import littleware.asset.AssetException;
import littleware.base.BaseException;
import littleware.base.DataAccessException;
import littleware.security.AccessDeniedException;


/**
 * Manager helps setup "sessions" that track the
 * authentication status of a set of interactions
 * with a particular principal.
 * When a principal authenticates itself - 
 * a new session-type Asset gets setup for that principal,
 * and a Remote SessionHelper is returned to RMI based clients
 * to interact with.
 * Non-RMI clients can use the session-asset's id as a handle
 * to access the SessionHelper associated with the session too.
 */
public interface SessionManager extends Remote {
						
	/**
	 * Get the SessionHelper associated with the Principal
	 * with the given credentials.
	 *
	 * @param s_name
	 * @param s_password
	 * @param s_session_comment briefly describing the purpose of this new login session
	 */
	public SessionHelper  login ( String s_name,
								  String s_password,
								  String s_session_comment
								  ) throws BaseException, AssetException, 
	GeneralSecurityException, RemoteException;
	
	/**
	 * Access the session with the given id.
	 * We leave it up to clients to keep their session-id's secret as best they can.
	 * This method allows a user to easily launch scripts/etc. across his network
	 * that authenticate themselves based on the user's login session.
	 * However, the user must periodically re-authenticate himself to
	 * keep the Session active, otherwise the session eventually times out,
	 * and any applications relying on the session will be denied access to the 
	 * data repository.
	 */
	public SessionHelper getSessionHelper ( UUID u_session
											) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException;
	
	/**
	 * Get the URL of the manager we're connected to
	 */
	public URL getUrl () throws RemoteException;

}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

