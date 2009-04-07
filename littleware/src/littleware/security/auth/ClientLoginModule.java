package littleware.security.auth;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.security.acl.*;
import javax.security.auth.*;
import javax.security.auth.spi.*;
import javax.security.auth.login.*;
import javax.security.auth.callback.*;


import littleware.base.*;
import littleware.asset.*;
import littleware.security.*;

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

    private static Logger olog_generic = Logger.getLogger("littleware.security.auth.ClientLoginModule");
    public final static String S_ACL_OPTION_NAME = "acl_check";
    public final static String S_SESSION_LOGIN_NAME = "littleware_session_id";
    private CallbackHandler ox_handler = null;
    private Subject oj_subject = null;
    private Set<String> ov_acl = new HashSet<String>();
    private final SessionManager om_session;

    /**
     * Constructor just pulls a SessionManager out of the default SessionUtil
     */
    public ClientLoginModule() {
        try {
            om_session = SessionUtil.get().getSessionManager();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new AssertionFailedException("Failed to establish SessionManager", e);
        }
    }

    /**
     * Constructor accepts user-supplied SessionManager
     */
    public ClientLoginModule(SessionManager m_session) {
        om_session = m_session;
    }

    /**
     * Initialize the module with data from underlying
     * login context
     *
     * @param j_subject to manage
     * @param x_handler to invoke for user-supplied data
     * @param v_shared_state map shared with other login modules
     * @param v_options login options - currently only look for acl_check
     */
    @Override
    public void initialize(Subject j_subject,
            CallbackHandler x_handler,
            Map v_shared_state,
            Map v_options) {
        oj_subject = j_subject;
        ox_handler = x_handler;
        try {
            String s_acl_list = (String) v_options.get(S_ACL_OPTION_NAME);

            if (null != s_acl_list) {
                for (StringTokenizer token_comma = new StringTokenizer(s_acl_list, ", ");
                        token_comma.hasMoreTokens();) {
                    ov_acl.add(token_comma.nextToken());
                }
            }

        } catch (Exception e) {
            olog_generic.log(Level.WARNING, "Failure retrieving LoginModule options, caught: " + e +
                    ", " + BaseException.getStackTrace(e));
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
        if (null == ox_handler) {
            throw new LoginException("No CallbackHandler registered with module");
        }
        if (null == oj_subject) {
            throw new LoginException("Subject never setup");
        }

        String s_user = null;
        String s_password = null;

        try {
            // Collect username and password via callbacks
            Callback[] v_callbacks = {
                new NameCallback("Enter username"),
                new PasswordCallback("Enter password", false)
            };
            ox_handler.handle(v_callbacks);

            s_user = ((NameCallback) v_callbacks[ 0]).getName();
            s_password = new String(((PasswordCallback) v_callbacks[ 1]).getPassword());

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new LoginException("Failure handling callbacks, caught: " + e);
        }

        try {
            SessionHelper m_helper = null;

            if (s_user.equals(S_SESSION_LOGIN_NAME)) {
                // Then the password is a session-id
                m_helper = om_session.getSessionHelper(UUIDFactory.parseUUID(s_password));
            } else {
                m_helper = om_session.login(s_user, s_password,
                        "ClientLoginModule login");
            }

            AssetSearchManager m_search = m_helper.getService(ServiceType.ASSET_SEARCH);

            LittleUser user_active = m_helper.getSession().getCreator(m_search);

            oj_subject.getPrincipals().add(user_active);
            // Also add a TomcatUser - the Tomcat Realm system holds onto Principals, not Subjects
            oj_subject.getPrincipals().add(new TomcatUser(user_active, m_helper));
            oj_subject.getPublicCredentials().add(m_helper);

            if (!ov_acl.isEmpty()) {
                // Check the user's access to ACL's, and add derivative roles to Subject Principal set
                Set<LittlePermission> v_perms = LittlePermission.getMembers();

                for (String s_acl_name : ov_acl) {
                    try {
                        Acl acl_check = (LittleAcl) m_search.getByName(s_acl_name,
                                SecurityAssetType.ACL);
                        if ( null != acl_check ) {
                            for (LittlePermission perm_check : v_perms) {
                                if (acl_check.checkPermission(user_active, perm_check)) {
                                    String s_role = s_acl_name + ":" + perm_check.toString();
                                    olog_generic.log(Level.INFO, "For user " + user_active.getName() +
                                            " adding role: " + s_role);
                                    oj_subject.getPrincipals().add(new SimpleRole(s_role));
                                }
                            }
                        }
                    } catch (RuntimeException e) {
                        throw e;
                    } catch (Exception e) {
                        olog_generic.log(Level.WARNING, "Failure loading acl: " + s_acl_name +
                                " for user " + user_active.getName() + " login, caught: " +
                                e + ", " + BaseException.getStackTrace(e));
                    // keep going
                    }
                }
            }

            olog_generic.log(Level.INFO, "User authenticated: " + user_active.getName());

        } catch (RuntimeException e) {
            throw e;
        } catch (LoginException e) {
            throw e;
        } catch (Exception e) {
            olog_generic.log(Level.WARNING, "Authentication of " + s_user + "failed, caught: " + e);
            throw new FailedLoginException("Authentication of " + s_user + " failed, caught: " + e);
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

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


