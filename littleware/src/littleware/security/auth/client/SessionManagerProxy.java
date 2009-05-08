/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.security.auth.client;

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

    private static final Logger olog_generic = Logger.getLogger(SessionManagerProxy.class.getName());
    private SessionManager om_session = null;
    private URL ourl_session = null;
    /** Setup RemoteException handler */
    private RemoteExceptionHandler ohandler_remote = new RemoteExceptionHandler() {

        /**
         * Try to lookup the freaking SessionManager on the name server again
         * to rebootstrap a session on a server restart.
         */
        @Override
        public void handle(RemoteException e_remote) throws RemoteException {
            super.handle(e_remote);
            try {
                om_session = SessionUtil.get ().getSessionManager(
                        ourl_session.getHost(),
                        ourl_session.getPort());
            } catch (Exception e) {
                olog_generic.log(Level.WARNING, "Retry handler failed to reconnect to down SessionManager, caught: " +
                        e + ", " + BaseException.getStackTrace(e));
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
    public SessionManagerProxy(SessionManager m_session, URL url_session) {
        om_session = m_session;
        ourl_session = url_session;
    }

    @Override
    public SessionHelper login(String s_name,
            String s_password,
            String s_session_comment) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        while (true) {
            try {
                SessionHelper m_helper = om_session.login(s_name, s_password,
                        s_session_comment);
                return new SessionHelperProxy(m_helper, this,
                        m_helper.getSession().getObjectId());
            } catch (RemoteException e) {
                ohandler_remote.handle(e);
            }
        }
    }

    public SessionHelper getSessionHelper(UUID u_session) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        RemoteException e_last = null;

        while (true) {
            try {
                SessionHelper m_helper = om_session.getSessionHelper(u_session);

                return new SessionHelperProxy(m_helper, this, u_session);
            } catch (RemoteException e) {
                ohandler_remote.handle(e);
            }
        }
    }

    public URL getUrl() throws RemoteException {
        return ourl_session;
    }
}
