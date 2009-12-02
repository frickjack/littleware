/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.security.auth.server;

import java.rmi.RemoteException;
//import java.rmi.server.UnicastRemoteObject;
import java.security.GeneralSecurityException;

import littleware.asset.AssetException;
import littleware.asset.client.LittleService;
import littleware.base.BaseException;
import littleware.base.LittleRemoteObject;
import littleware.security.auth.*;

/**
 * Straight forward implementation of SessionHelper - 
 * deploys RMI-enabled managers wrapping timeout/read-only
 * aware proxies of standard Manager implementations.
 */
public class RmiSessionHelper extends LittleRemoteObject implements SessionHelper {
    private static final long serialVersionUID = 3828770977132694491L;

    SessionHelper om_helper = null;

    /**
     * Constructor sets up a SessionInvocationHandler based wrapper
     * around the given helper implementation.
     */
    public RmiSessionHelper(SessionHelper m_helper) throws RemoteException {
        //super( littleware.security.auth.SessionUtil.getRegistryPort() );        
        om_helper = m_helper;
    }

    public LittleSession getSession() throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return om_helper.getSession();
    }

    public <T extends LittleService> T getService(ServiceType<T> n_type) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        /*..
        if ( n_type.equals( ServiceType.SESSION_HELPER ) ) {
            // Need to do this hook when running in same JVM
            return (T) this;
        }
         */
        return om_helper.getService(n_type);
    }

    /**
     * Create a new session for this user
     */
    public SessionHelper createNewSession(String s_session_comment)
            throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return om_helper.createNewSession(s_session_comment);
    }
}

