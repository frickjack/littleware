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
    private CallbackHandler handler = null;
    private Subject subject = null;
    private Set<String> aclNameList = new HashSet<String>();
    private SessionManager sessionManager = null;

    /**
     * Constructor just pulls a SessionManager out of the default SessionUtil
     */
    public ClientLoginModule() {
        try {
            sessionManager = SessionUtil.get().getSessionManager();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new AssertionFailedException("Failed to establish SessionManager", e);
        }
    }

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
                new NameCallback("Enter username"),
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
        UUID sessionId = null;

        try {
            sessionId = UUIDFactory.parseUUID(userName);
        } catch (Exception ex) {
        }

        try {
            final SessionHelper helper;
            if (null != sessionId) {
                // Then the password is a session-id
                helper = sessionManager.getSessionHelper(UUIDFactory.parseUUID(password));
            } else {
                helper = sessionManager.login(userName, password,
                        "ClientLoginModule login");
            }

            final AssetSearchManager search = helper.getService(ServiceType.ASSET_SEARCH);

            final LittleUser user = search.getAsset(helper.getSession().getCreatorId()).get().narrow();

            subject.getPrincipals().add(user);
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

            log.log(Level.INFO, "User authenticated: " + user.getName());

        } catch (RuntimeException e) {
            throw e;
        } catch (LoginException e) {
            throw e;
        } catch (Exception e) {
            log.log(Level.WARNING, "Authentication of " + userName + "failed, caught: " + e);
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
}

