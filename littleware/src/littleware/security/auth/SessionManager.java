/*
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.security.auth;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;
import java.security.GeneralSecurityException;

import littleware.asset.AssetException;
import littleware.base.BaseException;


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
	
}

