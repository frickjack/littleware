/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.security.auth.server.internal;

import java.rmi.RemoteException;
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

    private final SessionHelper coreHelper;

    /**
     * Constructor sets up a SessionInvocationHandler based wrapper
     * around the given helper implementation.
     */
    public RmiSessionHelper(SessionHelper helper) throws RemoteException {
        coreHelper = helper;
    }

    @Override
    public LittleSession getSession() throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return coreHelper.getSession();
    }

    @Override
    public <T extends LittleService> T getService(ServiceType<T> serviceType ) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        /*..
        if ( n_type.equals( ServiceType.SESSION_HELPER ) ) {
            // Need to do this hook when running in same JVM
            return (T) this;
        }
         */
        return coreHelper.getService(serviceType );
    }

    /**
     * Create a new session for this user
     */
    @Override
    public SessionHelper createNewSession(String s_session_comment)
            throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return coreHelper.createNewSession(s_session_comment);
    }

    @Override
    public String getServerVersion() throws RemoteException {
        return coreHelper.getServerVersion();
    }
}

