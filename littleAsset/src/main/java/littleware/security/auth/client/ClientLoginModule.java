/*
 * Copyright 2011 http://code.google.com/p/littleware
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security.auth.client;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.security.auth.*;
import javax.security.auth.spi.*;
import javax.security.auth.login.*;
import javax.security.auth.callback.*;
import littleware.asset.AssetException;
import littleware.asset.client.AssetManager;


import littleware.base.*;
import littleware.security.LittleUser;
import littleware.security.auth.LittleSession;

/**
 * Login module for littleware clients.
 * Gains a SessionManager via SessionUtil, and
 * logs in with the supplied credentials.
 * Accepts a set of ACLs at init time to check user access against -
 * adds Roles to the Subject Principal set based if
 * the user has permissions - ex: acl_name:littleware.READ.
 * The option name is acl_check: <br />
 *        acl_check="acl_name1,acl_name2" <br />
 * The Principal type is LittleUser, the Role type is LittleRole,
 * and the session UUID is registered an the "session_id" public credential.
 * Finally, a user may login with an active session-id by specifying
 *       "littleware_session_id"
 * in the user-name callback, and supplying the session-id as the password.
 */
public class ClientLoginModule implements LoginModule {

    private static final Logger log = Logger.getLogger(ClientLoginModule.class.getName());
    public final static String ACL_OPTION = "acl_check";
    public final static String HOST_OPTION = "host";
    public final static String PORT_OPTION = "port";
    public final static String CACHE_OPTION = "cache";
    public final static String MANAGER_OPTION = "sessionManager";
    public final static String SAVER_OPTION = "assetManager";

    private CallbackHandler handler = null;
    private SessionManager sessionManager = null;
    private Subject subject = null;
    private Set<String> aclNameList = new HashSet<String>();
    private boolean useCache = false;
  private AssetManager assetSaver;

    /**
     * Initialize the module with data from underlying
     * login context
     *
     * @param subject to manage
     * @param handler to invoke for user-supplied data
     * @param sharedState map shared with other login modules
     * @param optionsMap login options - currently only look for acl_check
     */
    @Override
    public void initialize(Subject subject,
            CallbackHandler handler,
            Map sharedState,
            Map optionsMap) {
        this.subject = subject;
        this.handler = handler;
        this.sessionManager = (SessionManager) optionsMap.get( MANAGER_OPTION );
        this.assetSaver = (AssetManager) optionsMap.get( SAVER_OPTION );
        final String host = (String) optionsMap.get(HOST_OPTION);
        final String port = (String) optionsMap.get(PORT_OPTION);
        final String cacheString = (String) optionsMap.get(CACHE_OPTION);

        if (null != cacheString) {
            final String cacheClean = cacheString.trim().toLowerCase();
            log.log(Level.FINE, "Considering cache option string: {0}", cacheClean);
            if (cacheClean.startsWith("on")
                    || cacheClean.startsWith("yes")
                    || cacheClean.startsWith("true")) {
                useCache = true;
            }
        }

        /*
        try {
        final SessionUtil util = SessionUtil.get();
        if (null != host) {
        if (null != port) {
        this.sessionManager = util.getSessionManager(host, Integer.parseInt(port));
        } else {
        this.sessionManager = util.getSessionManager(host, util.getRegistryPort());
        }
        } else {
        this.sessionManager = util.getSessionManager();
        }
        } catch (Exception ex) {
        throw new IllegalStateException("Failed to setup SessionManager", ex);
        }
         *
         */
        try {
            final String aclListString = (String) optionsMap.get(ACL_OPTION);

            if (null != aclListString) {
                for (final StringTokenizer tokenizer = new StringTokenizer(aclListString, ", ");
                        tokenizer.hasMoreTokens();) {
                    aclNameList.add(tokenizer.nextToken());
                }
            }

        } catch (Exception ex) {
            throw new IllegalArgumentException("Failure retrieving LoginModule options", ex);
        }
    }
    private static final String cacheFileName = "latest_session.properties";
    private static final String cacheUserKey = "session.username";
    private static final String cacheSessionKey = "session.id";

    /**
     * Attempt phase-1 login using cached CallbackHandler to get user info
     *
     * @return true if authentication succeeds, false to ignore this module
     * @throws LoginException if authentication fails
     */
    @Override
    public boolean login() throws LoginException {
        if (null == handler) {
            throw new LoginException("No CallbackHandler registered with module");
        }
        if (null == subject) {
            throw new LoginException("Subject never setup");
        }

        String userName = null;
        String password = null;

        try {
            // Collect username and password via callbacks
            final Callback[] callbacks = {
                new NameCallback("Enter username", System.getProperty("user.name", "username")),
                new PasswordCallback("Enter password", false)
            };
            handler.handle(callbacks);

            userName = ((NameCallback) callbacks[ 0]).getName();
            password = new String(((PasswordCallback) callbacks[ 1]).getPassword());

        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw (LoginException) (new LoginException("Failure handling callbacks")).initCause(ex);
        }

        UUID sessionId = null;
        boolean sessionIdFromUserName = false;
        try {
            sessionId = UUIDFactory.parseUUID(userName);
            sessionIdFromUserName = true;
        } catch (Exception ex) {
        }

        // Try to re-use an existing login session if possible
        // to re-use server-side resources
        final String loggedInUser = System.getProperty("user.name");
        final File cacheFile = new File(Whatever.Folder.LittleHome.getFolder(), cacheFileName);

        if (
                (null == sessionId)
                && loggedInUser.equals(userName)
                && cacheFile.exists()
                )
        {
            String sessionInCache = null;
            String userInCache = null;

            final Properties props = new Properties();
            Reader cacheReader = null;
            try {
                cacheReader = new FileReader(cacheFile);
                props.load(cacheReader);
                sessionInCache = props.getProperty(cacheSessionKey);
                userInCache = props.getProperty(cacheUserKey);
                cacheFile.delete();
            } catch (IOException ex) {
                log.log(Level.FINE, "Unable to load " + cacheFileName + ", proceeding ...");
            } finally {
                Whatever.get().close(cacheReader);
            }
            if ((null != sessionInCache) && (null != userInCache) && userInCache.equals(userName)) {
                try {
                    sessionId = UUIDFactory.parseUUID(sessionInCache);
                } catch (Exception ex) {
                }
            }
        }

        SessionManager.Credentials creds = null;
        if ( null != sessionId ) {
            // attempt to use an already existing session
            try {
                creds = sessionManager.login( sessionId );
            } catch ( Exception ex ) {
                log.log( Level.FINE, "Failed to login with sessionId: {0}", sessionId);
                if ( sessionIdFromUserName ) {
                    throw (LoginException) (new FailedLoginException("Failed session " + sessionId + " login").initCause(ex));
                }
            }
        }

        if (null == creds) {
            try {
                creds = sessionManager.login(userName, password,
                        "ClientLoginModule login");
            } catch (Exception ex) {
                throw (LoginException) (new FailedLoginException("Failed " + userName + " login").initCause(ex));
            }
        }
        if (userName.equals(loggedInUser)) {
            try { // Update session cache
                final Properties props = new Properties();
                props.setProperty(cacheUserKey, userName);
                props.setProperty(cacheSessionKey, UUIDFactory.makeCleanString( creds.getSession().getId() ));
                PropertiesLoader.get().safelySave(props, cacheFile);
            } catch (Exception ex) {
                log.log(Level.WARNING, "Failed to update session cache", ex);
            }
        }

        // finaly - decorate the authenticated Subject
        try {
            subject.getPrincipals().add( creds.getUser() );
            subject.getPublicCredentials().add( creds.getSession() );
            //subject.getPrivateCredentials().add(helper);

            if (!aclNameList.isEmpty()) {
                log.log( Level.WARNING, "Ignoring aclNameList for now ..." );
            }
            log.log(Level.INFO, "User authenticated: {0}", creds.getUser().getName());
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            log.log(Level.WARNING, "Authentication of " + userName + "failed", ex);
            throw (LoginException) (new FailedLoginException("Authentication of " + userName + " failed").initCause(ex));
        }
        return true;
    }

    /**
     * Phase 2 commit of login.
     * Idea is that multiple modules may go through a phase 1 login,
     * then phase 2 comes through once all is ok.
     *
     * @throws LoginException if commit fails
     */
    @Override
    public boolean commit() throws LoginException {
        return true;
    }

    /**
     * Abort the login process - always returns true for now -
     * should cancel out the LittleSession later.
     *
     * @throws LoginException if abort fails
     */
    @Override
    public boolean abort() {
        return true;
    }

    /**
     * Logout the subject associated with this module's context.
     * Sets end-date on LittleSession which effectively disables
     * access to littleware's backend - at least until the user reauthenticates.
     *
     * @return true if logout ok, false to ignore this module
     * @throws LoginException if logout fails
     */
    @Override
    public boolean logout() throws LoginException {
      // can only logout the user associated with this session
      final Option<SessionManager.Credentials> optCreds = sessionManager.getCredentials();
      if ( ! optCreds.isEmpty() ) {
        // end the session
        final LittleSession ls = optCreds.get().getSession().copy().endDate( new Date() ).build();
        try {
          assetSaver.saveAsset(ls, "JAAS logout" );
        } catch (BaseException  | GeneralSecurityException | RemoteException ex) {
          log.log(Level.INFO, "Failed to update session for logout ...", ex);
        }
      }
      return true;
    }

    /**
     * Simplify process of setting up a LoginContext Configuration
     * in simple apps.
     */
    public static interface ConfigurationBuilder {

        public ConfigurationBuilder host(String value);

        public ConfigurationBuilder port(int value);

        public ConfigurationBuilder useCache(boolean value);

        public Configuration build();
    }

}
