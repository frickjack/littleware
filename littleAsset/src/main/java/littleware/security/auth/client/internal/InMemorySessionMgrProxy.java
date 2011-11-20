/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security.auth.client.internal;

import com.google.inject.Inject;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import littleware.base.BaseException;
import littleware.security.auth.LittleSession;
import littleware.security.auth.internal.RemoteSessionManager;


public class InMemorySessionMgrProxy implements RemoteSessionMgrProxy {
    private final RemoteSessionManager delegate;
    
    @Inject
    public InMemorySessionMgrProxy( RemoteSessionManager delegate ) {
        this.delegate = delegate;
    }
    
    
    @Override
    public LittleSession login(String userName, String password, String comment) throws BaseException, GeneralSecurityException, RemoteException {
        return delegate.login( userName, password, comment );
    }

    @Override
    public LittleSession createNewSession(UUID currentSessionId, String sessionComment) throws BaseException, GeneralSecurityException, RemoteException {
        return delegate.createNewSession(currentSessionId, sessionComment);
    }

    @Override
    public String getServerVersion() throws RemoteException {
        return delegate.getServerVersion();
    }
    
}
