/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.security.auth.client.internal;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import littleware.base.BaseException;
import littleware.net.RemoteRetryHelper;
import littleware.security.auth.LittleSession;
import littleware.security.auth.internal.RemoteSessionManager;

/**
 * RemoteSessionManager implementation with built in retry logic
 */
@Singleton
public class RetryRemoteSessionMgr extends RemoteRetryHelper<RemoteSessionManager> implements RemoteSessionMgrProxy {

    @Inject
    public RetryRemoteSessionMgr( @Named("littleware.jndi.prefix") String jndiPrefix ) {
        super(jndiPrefix + RemoteSessionManager.LOOKUP_PATH );
    }

    @Override
    public LittleSession login(String userName, String password, String comment) throws BaseException, GeneralSecurityException, RemoteException {
        while (true) {
            try {
                return getLazy().login(userName, password,
                        comment
                        );
            } catch (RemoteException ex) {
                handle(ex);
            } catch (NullPointerException ex) {
                handle(new RemoteException("Unexpected exception", ex));
            }
        }
    }

    @Override
    public LittleSession createNewSession(UUID currentSessionId, String sessionComment) throws BaseException, GeneralSecurityException, RemoteException {
        while (true) {
            try {
                return getLazy().createNewSession( currentSessionId, sessionComment );
            } catch (RemoteException ex) {
                handle(ex);
            } catch (NullPointerException ex) {
                handle(new RemoteException("Unexpected exception", ex));
            }
        }
    }

    @Override
    public String getServerVersion() throws RemoteException {
        while (true) {
            try {
                return getLazy().getServerVersion();
            } catch (RemoteException ex) {
                handle(ex);
            } catch (NullPointerException ex) {
                handle(new RemoteException("Unexpected exception", ex));
            }
        }

    }

}
