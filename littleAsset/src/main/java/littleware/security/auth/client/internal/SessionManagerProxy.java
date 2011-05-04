/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security.auth.client.internal;

import com.google.inject.Inject;
import java.util.UUID;
import java.rmi.RemoteException;
import java.util.logging.Logger;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.Collections;
import javax.security.auth.Subject;
import littleware.asset.AssetException;
import littleware.asset.client.AssetSearchManager;
import littleware.asset.internal.RemoteSearchManager;
import littleware.base.BaseException;
import littleware.base.Maybe;
import littleware.base.Option;
import littleware.security.LittleUser;
import littleware.security.auth.LittleSession;
import littleware.security.auth.client.KeyChain;
import littleware.security.auth.client.SessionManager;
import littleware.security.auth.internal.RemoteSessionManager;

/** 
 * Little wrapper around SessionManager to facilitate 
 * transparent reconnect attempts on RemoteException.
 */
public class SessionManagerProxy implements SessionManager {

    private static final Logger log = Logger.getLogger(SessionManagerProxy.class.getName());
    private final RemoteSessionManager remote;
    private final RemoteSearchManager rsearch;
    private final AssetSearchManager search;
    private final KeyChain keychain;

    /**
     * Stash the wrapped manager and the URL it came from
     */
    @Inject
    public SessionManagerProxy(RemoteSessionManager remote,
            RemoteSearchManager rsearch,
            AssetSearchManager search,
            KeyChain keychain) {
        this.remote = remote;
        this.rsearch = rsearch;
        this.search = search;
        this.keychain = keychain;
    }

    @Override
    public Credentials login(String userName,
            String password,
            String sessionComment) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        final LittleSession session = remote.login(userName, password, sessionComment);
        final LittleUser user = rsearch.getAsset(session.getId(), session.getOwnerId()).get().narrow();
        keychain.setDefaultSessionId(session.getId());
        return new Creds(session, user);
    }

    @Override
    public LittleSession createNewSession(String sessionComment) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return remote.createNewSession(keychain.getDefaultSessionId().get(), sessionComment);
    }

    @Override
    public String getServerVersion() throws RemoteException {
        return remote.getServerVersion();
    }

    private static final Option<Credentials> emptyCreds = Maybe.empty();

    @Override
    public Option<Credentials> getCredentials() {
        final Option<UUID> key = keychain.getDefaultSessionId();
        if ( key.isEmpty() ) {
            return emptyCreds;
        }
        try {
            final LittleSession session = search.getAsset( key.get() ).get().narrow();
            final LittleUser    user = search.getAsset( session.getOwnerId() ).get().narrow();
            return Maybe.something( (Credentials) new Creds( session, user ) );
        } catch ( RuntimeException ex ) {
            throw ex;
        } catch ( Exception ex ) {
            throw new IllegalStateException( "Failed to load credentials", ex );
        }
    }

    @Override
    public Credentials login(UUID sessionId) throws BaseException, GeneralSecurityException, RemoteException {
        final LittleSession session = rsearch.getAsset( sessionId, sessionId ).get().narrow();
        final LittleUser user = rsearch.getAsset(session.getId(), session.getOwnerId()).get().narrow();
        keychain.setDefaultSessionId(session.getId());
        return new Creds(session, user);
    }


    //------------------------------------------------
    private static class Creds implements Credentials {

        private final LittleSession session;
        private final LittleUser user;
        private final Subject subject;

        public Creds(LittleSession session, LittleUser user) {
            this.session = session;
            this.user = user;
            this.subject = new Subject(false, Collections.singleton((Principal) user),
                    Collections.singleton(session), Collections.emptySet());
        }

        @Override
        public LittleSession getSession() {
            return session;
        }

        @Override
        public LittleUser getUser() {
            return user;
        }

        @Override
        public Subject getSubject() {
            return subject;
        }
    }
}
