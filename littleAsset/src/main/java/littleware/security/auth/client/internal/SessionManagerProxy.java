package littleware.security.auth.client.internal;

import com.google.inject.Inject;
import java.util.UUID;
import java.rmi.RemoteException;
import java.util.logging.Logger;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.Collections;
import java.util.Optional;
import javax.security.auth.Subject;
import littleware.asset.AssetException;
import littleware.asset.client.AssetSearchManager;
import littleware.asset.client.internal.RemoteSearchMgrProxy;
import littleware.asset.client.spi.AssetLoadEvent;
import littleware.asset.client.spi.LittleServiceBus;
import littleware.asset.internal.RemoteSearchManager;
import littleware.base.BaseException;
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
    private final LittleServiceBus eventBus;

    /**
     * Stash the wrapped manager and the URL it came from
     */
    @Inject
    public SessionManagerProxy(RemoteSessionMgrProxy remote,
            RemoteSearchMgrProxy rsearch,
            AssetSearchManager search,
            KeyChain keychain,
            LittleServiceBus eventBus ) {
        this.remote = remote;
        this.rsearch = rsearch;
        this.search = search;
        this.keychain = keychain;
        this.eventBus = eventBus;
    }

    @Override
    public Credentials login(String userName,
            String password,
            String sessionComment) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        final LittleSession session = remote.login(userName, password, sessionComment);
        final LittleUser user = rsearch.getAsset(session.getId(), session.getOwnerId(), -1L ).getAsset().get().narrow();
        keychain.setDefaultSessionId(session.getId());
        eventBus.fireEvent( new AssetLoadEvent( this, session ) );
        eventBus.fireEvent( new AssetLoadEvent( this, user ) );
        return new Creds(session, user);
    }

    @Override
    public LittleSession createNewSession(String sessionComment) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        final LittleSession result = remote.createNewSession(keychain.getDefaultSessionId().get(), sessionComment);
        eventBus.fireEvent( new AssetLoadEvent( this, result ) );
        return result;
    }

    @Override
    public String getServerVersion() throws RemoteException {
        return remote.getServerVersion();
    }

    private static final Optional<Credentials> emptyCreds = Optional.empty();

    @Override
    public Optional<Credentials> getCredentials() {
        final Optional<UUID> key = keychain.getDefaultSessionId();
        if ( ! key.isPresent() ) {
            return emptyCreds;
        }
        try {
            final LittleSession session = search.getAsset( key.get() ).get().narrow();
            final LittleUser    user = search.getAsset( session.getOwnerId() ).get().narrow();
            return Optional.ofNullable( (Credentials) new Creds( session, user ) );
        } catch ( RuntimeException ex ) {
            throw ex;
        } catch ( Exception ex ) {
            throw new IllegalStateException( "Failed to load credentials", ex );
        }
    }

    @Override
    public Credentials login(UUID sessionId) throws BaseException, GeneralSecurityException, RemoteException {
        final LittleSession session = rsearch.getAsset( sessionId, sessionId, -1L ).getAsset().get().narrow();
        final LittleUser user = rsearch.getAsset(session.getId(), session.getOwnerId(), -1L ).getAsset().get().narrow();
        keychain.setDefaultSessionId(session.getId());
        eventBus.fireEvent( new AssetLoadEvent( this, session ) );
        eventBus.fireEvent( new AssetLoadEvent( this, user ) );        
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
