package littleware.security.auth.client;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Optional;
import java.util.UUID;
import javax.security.auth.Subject;

import littleware.base.BaseException;
import littleware.security.LittleUser;
import littleware.security.auth.LittleSession;

/**
 * Manager helps setup "sessions" that track the
 * authentication status of a set of interactions
 * with a particular principal.
 * When a principal authenticates itself - 
 * a new session-type Asset gets setup for that principal.
 */
public interface SessionManager extends Remote {

    public interface Credentials {
        public LittleSession getSession();
        public LittleUser    getUser();
        /**
         * Returns read/write Subject with LittleUser in principal set,
         * and LittleSession in public credentials.
         */
        public Subject       getSubject();
    }
    /**
     * Login and retrieve Credentials for the given user,
     * and register the credential's session with the KeyChain.
     * Currently only allowed to login as a single user.
     * Currently only interact with one remote server (the default server from littleware.properties)
     *
     * @param comment briefly describing the purpose of this new login session
     * @return Subject with LittleUser principals and LittleSession public credentials
     */
    public Credentials login(String userName,
            String password,
            String comment) throws BaseException,
            GeneralSecurityException, RemoteException;

    /**
     * Retrieve credentials for the given session id,
     * and register the session with the KeyChain.
     * Throws SessionExpiredException if session is no longer valid.
     *
     * @param sessionId
     * @return Credentials
     * @throws BaseException
     * @throws GeneralSecurityException
     * @throws RemoteException
     */
    public Credentials login( UUID sessionId ) throws BaseException,
            GeneralSecurityException, RemoteException;

    /**
     * Get the credentials associated with the current session.
     * The result is empty (no principals or credentials) until login is called.
     */
    public Optional<Credentials> getCredentials();

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

