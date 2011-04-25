/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security.auth.client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import javax.security.auth.Subject;

import littleware.base.BaseException;
import littleware.security.auth.LittleSession;

/**
 * Manager helps setup "sessions" that track the
 * authentication status of a set of interactions
 * with a particular principal.
 * When a principal authenticates itself - 
 * a new session-type Asset gets setup for that principal.
 */
public interface SessionManager extends Remote {

    /**
     * Get the SessionHelper associated with the Principal
     * with the given credentials.
     * Currently only allowed to login as a single user.
     * Currently only interact with one remote server (the default server from littleware.properties)
     *
     * @param comment briefly describing the purpose of this new login session
     * @return Subject with LittleUser principals and LittleSession public credentials
     */
    public Subject login(String userName,
            String password,
            String comment) throws BaseException,
            GeneralSecurityException, RemoteException;

    /**
     * Get the subject associated with the current session.
     * The Subject is empty (no principals or credentials) until login is called.
     */
    public Subject getSubject();

    /**
     * Create a new session for this user to handoff to some other app or whatever
     * 
     * @return new session for the user associated with currentSessionId
     */
    public LittleSession createNewSession( String sessionComment)
            throws BaseException,
            GeneralSecurityException, RemoteException;

    /**
     * Get the server-side version string.
     * Provides mechanism for long-running clients to decide whether
     * they need to restart with a new code base.
     * Just retrieves data string from /littleware.home/ServerVersion asset.
     */
    public String getServerVersion() throws RemoteException;

}

