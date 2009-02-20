/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
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
import java.security.GeneralSecurityException;

import littleware.asset.*;
import littleware.asset.client.LittleService;
import littleware.base.*;


/**
 * After a Principal has authenticated itself,
 * then that client is supplied with a SessionHelper
 * that maintains the user's authentication session.
 * The SessionHelper provides hooks to query and update session status,
 * and access various Managers.
 * The user must re-authenticate himself to
 * keep the Session active, otherwise the session times out
 * at the session asset's end-time,
 * and any applications relying on the session will be denied access to the 
 * data repository.
 * A user may further configure the SessionHelper by manipulating
 * its asset (see LittleSession).
 * For example, a read-only session will not allow clients access to
 * write-only interfaces (ex: AssetManager), and will provide
 * write-disabled implementations of interfaces exporting
 * both read and write methods (ex: AclManager).
 */
public interface SessionHelper extends Remote {
	/**
	 * Get the session asset this SessionHelper is associated with
	 *
	 * @exception SessionExpiredException if the session this helper is
	 *          associated with is no longer active
	 */
	public @ReadOnly LittleSession getSession () throws BaseException, AssetException, 
	GeneralSecurityException, RemoteException;
	
		
	/**
	 * Get the AssetManager setup to excute on behalf of this session's principal
	 *
	 * @param n_type of the service desired
	 * @return the service manager - caller should cast to appropriate type
	 * @exception SessionExpiredException if the session this helper is
	 *          associated with is no longer active
	 */
	public @ReadOnly <T extends LittleService> T getService ( ServiceType<T> n_type ) throws BaseException, AssetException,
		GeneralSecurityException, RemoteException;
	
	/**
	 * Create a new session for this user
	 */
	public SessionHelper createNewSession ( String s_session_comment )
		throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException;
									
}

