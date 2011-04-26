/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.security.auth.client.internal;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.UUID;
import java.rmi.RemoteException;
import java.util.logging.Logger;
import java.security.GeneralSecurityException;
import javax.security.auth.Subject;
import littleware.asset.AssetException;
import littleware.base.BaseException;
import littleware.net.RemoteRetryHelper;
import littleware.security.auth.LittleSession;
import littleware.security.auth.client.SessionManager;
import littleware.security.auth.internal.RemoteSessionManager;

/** 
 * Little wrapper around SessionManager to facilitate 
 * transparent reconnect attempts on RemoteException.
 */
public class SessionManagerProxy extends RemoteRetryHelper<RemoteSessionManager> implements SessionManager {
    private static final Logger log = Logger.getLogger(SessionManagerProxy.class.getName());

    
    /**
     * Stash the wrapped manager and the URL it came from
     */
    @Inject
    public SessionManagerProxy( @Named( "littleware.jndi.prefix" ) String jndiPrefix ) {
        super( jndiPrefix + "/littleware/SessionManager" );
    }

    @Override
    public Subject login(String userName,
            String password,
            String sessionComment) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        while (true) {
            try {
                return getLazy().login(userName, password,
                        sessionComment);
            } catch (RemoteException ex ) {
                handle(ex );
            } catch ( NullPointerException ex ) {
                handle( new RemoteException( "Unexpected exception", ex ) );
            }
        }
    }

    @Override
    public LittleSession createNewSession(UUID currentSessionId, String sessionComment) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        while (true) {
            try {
                return getLazy().createNewSession( currentSessionId, sessionComment );
            } catch (RemoteException ex ) {
                handle(ex );
            }
        }
    }

    @Override
    public String getServerVersion() throws RemoteException {
        while (true) {
            try {
                return getLazy().getServerVersion();
            } catch (RemoteException ex ) {
                handle(ex );
            }
        }
    }

}
