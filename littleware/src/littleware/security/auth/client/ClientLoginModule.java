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

import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.security.auth.*;
import javax.security.auth.spi.*;
import javax.security.auth.login.*;
import javax.security.auth.callback.*;


import littleware.base.*;
import littleware.asset.*;
import littleware.security.*;
import littleware.security.auth.ServiceType;
import littleware.security.auth.SessionHelper;
import littleware.security.auth.SessionManager;
import littleware.security.auth.SessionUtil;

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
    private CallbackHandler handler = null;
    private SessionManager sessionManager = null;
    private Subject subject = null;
    private Set<String> aclNameList = new HashSet<String>();
    private boolean useCache = false;

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
        final String host = (String) optionsMap.get(HOST_OPTION);
        final String port = (String) optionsMap.get(PORT_OPTION);
        final String cacheString = (String) optionsMap.get(CACHE_OPTION);

        if (null != cacheString) {
            final String cacheClean = cacheString.trim().toLowerCase();
            log.log(Level.FINE, "Considering cache option string: " + cacheClean);
            if (cacheClean.startsWith("on")
                    || cacheClean.startsWith("yes")
                    || cacheClean.startsWith("true")) {
                useCache = true;
            }
        }

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
     * @exception LoginException if authentication fails
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

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new LoginException("Failure handling callbacks, caught: " + e);
        }

        boolean sessionIdFromUsername = false;
        UUID sessionId = null;

        try {
            sessionId = UUIDFactory.parseUUID(userName);
            sessionIdFromUsername = true;
        } catch (Exception ex) {
        }

        // Try to re-use an existing login session if possible
        // to re-use server-side resources
        final String loggedInUser = System.getProperty("user.name");
        final File cacheFile = new File(Whatever.Folder.LittleHome.getFolder(), cacheFileName);

        if ((null == sessionId) && loggedInUser.equals(userName) && cacheFile.exists() ) {
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
        SessionHelper helper = null;
        if (null != sessionId) {
            try {
                // Then the password is a session-id
                helper = sessionManager.getSessionHelper(sessionId);
            } catch (Exception ex) {
                if ( sessionIdFromUsername ) {
                    throw new FailedLoginException( "Failed to authenticate to session" );
                }
                log.log(Level.FINE, "Failed to login to cached session " + sessionId, ex);
            }
        }
        if (null == helper) {
            try {
                helper = sessionManager.login(userName, password,
                        "ClientLoginModule login");
            } catch (Exception ex) {
                throw new FailedLoginException("Failed " + userName + " login, caught: " + ex);
            }
        }
        if (userName.equals(loggedInUser)) {
            try { // Update session cache
                final Properties props = new Properties();
                props.setProperty(cacheUserKey, userName);
                props.setProperty(cacheSessionKey, UUIDFactory.makeCleanString(helper.getSession().getId()));
                PropertiesLoader.get().safelySave(props, cacheFile);
            } catch (Exception ex) {
                log.log(Level.WARNING, "Failed to update session cache", ex);
            }
        }
        // finaly - decorate the authenticated Subject
        try {
            final AssetSearchManager search = helper.getService(ServiceType.ASSET_SEARCH);
            final LittleUser user = search.getAsset(helper.getSession().getCreatorId()).get().narrow();

            subject.getPrincipals().add(
                    user);
            subject.getPrivateCredentials().add(helper);

            if (!aclNameList.isEmpty()) {
                // Check the user's access to ACL's, and add derivative roles to Subject Principal set
                final Set<LittlePermission> permissionSet = LittlePermission.getMembers();

                for (String aclName : aclNameList) {
                    try {
                        final Maybe<Asset> maybeAcl = search.getByName(aclName,
                                SecurityAssetType.ACL);
                        if (maybeAcl.isSet()) {
                            final LittleAcl acl_check = maybeAcl.get().narrow();
                            for (LittlePermission permissionCheck : permissionSet) {
                                if (acl_check.checkPermission(user, permissionCheck)) {
                                    final String roleName = aclName + ":" + permissionCheck.toString();
                                    log.log(Level.INFO, "For user " + user.getName()
                                            + " adding role: " + roleName);
                                    subject.getPrincipals().add(new SimpleRole(roleName));
                                }
                            }
                        }
                    } catch (RuntimeException e) {
                        throw e;
                    } catch (Exception e) {
                        log.log(Level.WARNING, "Failure loading acl: " + aclName
                                + " for user " + user.getName() + " login, caught: "
                                + e + ", " + BaseException.getStackTrace(e));
                        // keep going
                    }
                }
            }
            log.log(Level.INFO, "User authenticated: "
                    + user.getName());
        } catch (RuntimeException e) {
            throw e;
        } catch (LoginException e) {
            throw e;
        } catch (Exception e) {
            log.log(Level.WARNING, "Authentication of " + userName + "failed", e );
            throw new FailedLoginException("Authentication of " + userName + " failed, caught: " + e);
        }
        return true;
    }

    /**
     * Phase 2 commit of login.
     * Idea is that multiple modules may go through a phase 1 login,
     * then phase 2 comes through once all is ok.
     *
     * @exception LoginException if commit fails
     */
    @Override
    public boolean commit() throws LoginException {
        return true;
    }

    /**
     * Abort the login process - always returns true for now -
     * should cancel out the LittleSession later.
     *
     * @exception LoginException if abort fails
     */
    @Override
    public boolean abort() {
        return true;
    }

    /**
     * Logout the subject associated with this module's context.
     * Does nothing for now - should cancel out the LittleSession later.
     *
     * @return true if logout ok, false to ignore this module
     * @exception LoginException if logout fails
     */
    @Override
    public boolean logout() throws LoginException {
        return true;
    }

    private static class SimpleBuilder implements ConfigurationBuilder {

        private Map<String, String> optionMap = new HashMap<String, String>();

        @Override
        public ConfigurationBuilder host(String value) {
            optionMap.put(HOST_OPTION, value);
            return this;
        }

        @Override
        public ConfigurationBuilder port(int value) {
            optionMap.put(PORT_OPTION, Integer.toString(value));
            return this;
        }

        @Override
        public ConfigurationBuilder useCache(boolean value) {
            optionMap.put(CACHE_OPTION, value ? "true" : "false");
            return this;
        }

        @Override
        public Configuration build() {
            final AppConfigurationEntry[] entry = {
                new AppConfigurationEntry(ClientLoginModule.class.getName(),
                AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                ImmutableMap.copyOf(optionMap))
            };
            return new Configuration() {

                @Override
                public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
                    return entry;
                }
            };
        }
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
    /**
     * Little fatory method to allocate a simple
     * in-app ConfigurationBuilder to setup a LoginContext
     * configuration that uses the ClientLoginModule.
     */
    public static ConfigurationBuilder newBuilder() {
        return new SimpleBuilder();
    }
}
