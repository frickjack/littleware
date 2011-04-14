/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.security.auth.client.internal;

import littleware.security.auth.internal.SessionUtil;
import littleware.security.auth.*;
import java.rmi.RemoteException;
import java.net.URL;
import java.util.UUID;
import java.util.logging.Logger;
import java.security.GeneralSecurityException;

import java.util.logging.Level;
import littleware.base.*;
import littleware.asset.AssetException;

/** 
 * Little wrapper around SessionManager to facilitate 
 * transparent reconnect attempts on RemoteException.
 */
public class SessionManagerProxy implements SessionManager {

    private static final Logger log = Logger.getLogger(SessionManagerProxy.class.getName());
    private  SessionManager sessionMgr = null;
    private final URL sessionUrl;
    /** Setup RemoteException handler */
    private final RemoteExceptionHandler remoteExceptionHandler = new RemoteExceptionHandler() {

        /**
         * Try to lookup the freaking SessionManager on the name server again
         * to rebootstrap a session on a server restart.
         */
        @Override
        public void handle(RemoteException remoteEx) throws RemoteException {
            super.handle(remoteEx);
            try {
                sessionMgr = SessionUtil.get ().getSessionManager(
                        sessionUrl.getHost(),
                        sessionUrl.getPort());
            } catch (Exception ex) {
                log.log(Level.WARNING, "Retry handler failed to reconnect to down SessionManager", ex );
            }
            try { // sleep between retries
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    };
    
    /**
     * Stash the wrapped manager and the URL it came from
     */
    public SessionManagerProxy(SessionManager sessionMgr, URL sessionUrl ) {
        this.sessionMgr = sessionMgr;
        this.sessionUrl = sessionUrl ;
    }

    @Override
    public SessionHelper login(String userName,
            String password,
            String sessionComment) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        while (true) {
            try {
                final SessionHelper helper = sessionMgr.login(userName, password,
                        sessionComment);
                return new SessionHelperProxy(helper, this,
                        helper.getSession().getId());
            } catch (RemoteException ex ) {
                remoteExceptionHandler.handle(ex );
            } catch ( NullPointerException ex ) {
                remoteExceptionHandler.handle( new RemoteException( "Unexpected exception", ex ) );
            }
        }
    }

    @Override
    public SessionHelperProxy getSessionHelper(UUID sessionId) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        RemoteException lastException = null;

        while (true) {
            try {
                final SessionHelper helper = sessionMgr.getSessionHelper(sessionId);

                return new SessionHelperProxy(helper, this, sessionId);
            } catch (RemoteException ex ) {
                remoteExceptionHandler.handle(ex );
            } catch ( NullPointerException ex ) {
                remoteExceptionHandler.handle( new RemoteException( "Unexpected exception", ex ) );
            }
        }
    }

    public URL getUrl() throws RemoteException {
        return sessionUrl;
    }

}
