/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.security.auth.internal;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import littleware.base.BaseException;
import littleware.security.auth.LittleSession;

/**
 * Remote methods for SessionManager service
 */
public interface RemoteSessionManager extends Remote {

    /**
     * Get the SessionHelper associated with the Principal
     * with the given credentials.
     *
     * @param comment briefly describing the purpose of this new login session
     */
    public LittleSession login(String userName,
            String password,
            String comment) throws BaseException,
            GeneralSecurityException, RemoteException;

    /**
     * Create a new session for this user to handoff to some other app or whatever
     *
     * @return new session for the user associated with currentSessionId
     */
    public LittleSession createNewSession( UUID currentSessionId, String sessionComment)
            throws BaseException, 
            GeneralSecurityException, RemoteException;

    /**
     * Get the server-side version string.
     * Provides mechanism for long-running clients to decide whether
     * they need to restart with a new code base.
     * Just retrieves data string from /littleware.home/ServerVersion asset.
     */
    public String getServerVersion() throws RemoteException;

    /**
     * Position in JNDI or RMI directory to bind/lookup this service
     */
    public static final String  LOOKUP_PATH = "littleware/SessionManager";
}

