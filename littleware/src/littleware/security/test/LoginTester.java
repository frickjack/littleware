package littleware.security.test;

import java.util.*;
import java.security.Principal;
import javax.security.auth.*;
import javax.security.auth.login.*;
import java.security.GeneralSecurityException;
import java.util.logging.Logger;
import java.util.logging.Level;

import junit.framework.*;

import littleware.security.*;
import littleware.security.auth.*;
import littleware.asset.AssetManager;
import littleware.asset.AssetSearchManager;
import littleware.base.BaseException;

/**
 * Test case for active login module - just tries
 * to login the test_user user and a bogus user.
 */
public class LoginTester extends TestCase {

    private static Logger olog_generic = Logger.getLogger("littelware.security.test");
    public static final String OS_TEST_USER = "littleware.test_user";
    public static final String OS_TEST_USER_PASSWORD = "test123";
    public static final String OS_TEST_GROUP = "group.littleware.test_user";
    private String os_test_user;
    private String os_test_user_password;
    private SessionManager om_session = null;

    /**
     * Constructor registers the test_user login to test against.
     * Use littleware.security.test.LoginTester as LoginContext application name.
     * Assume the underlying LoginModule can handle Subject initialization
     * with Principal data of an appropriate type.
     *
     * @param s_name of test case to run
     * @param s_test_user user to login 
     * @param s_test_user_password
     * @param m_session SessionManager to use in testSessionSetup
     */
    public LoginTester(String s_name,
            String s_test_user,
            String s_test_user_password,
            SessionManager m_session) {
        super(s_name);
        os_test_user = s_test_user;
        os_test_user_password = s_test_user_password;
        om_session = m_session;
    }

    /** No setup necessary */
    public void setUp() {
    }

    /** No tearDown necessary */
    public void tearDown() {
    }

    /**
     * Export this method to allow other test fixtures to test login
     * on a given user/password.
     *
     * @param s_user to login
     * @param s_password to login
     * @param x_fixture to assert against
     * @return authenticated principal object
     */
    public static Principal runLoginTest(String s_user, String s_password, TestCase x_fixture) {
        Subject j_login = runLoginTest("littleware.security.simplelogin",
                s_user, s_password, x_fixture);
        return j_login.getPrincipals(LittleUser.class).iterator().next();
    }

    /**
     * Run test against a supplied LoginContext
     *
     * @param s_configuration to test against
     * @param s_user to login
     * @param s_password to login
     * @param x_fixture to assert against
     * @return authenticated Subject
     */
    public static Subject runLoginTest(String s_configuration,
            String s_user, String s_password, TestCase x_fixture) {
        try {
            LoginContext x_login = new LoginContext(s_configuration,
                    new SimpleNamePasswordCallbackHandler(s_user, s_password));
            x_login.login();

            Subject x_subject = x_login.getSubject();
            x_fixture.assertTrue("Login subject has non-empty principal set",
                    !x_subject.getPrincipals().isEmpty());

            Set<LittleUser> v_principals = x_subject.getPrincipals(LittleUser.class);
            x_fixture.assertTrue("Subject has exactly 1 LittleUser principal: " + v_principals.size(),
                    1 == v_principals.size());
            LittleUser x_user = (LittleUser) v_principals.iterator().next();
            x_fixture.assertTrue("Principal has wrong name: " + s_user + " != " + x_user.getName(),
                    x_user.getName().equals(s_user));

            olog_generic.log(Level.INFO, "LoginTester successfully completed " + s_user + " login");
            return x_subject;
        } catch (NoSuchElementException e) {
            x_fixture.assertTrue("Caught unexpected: " + e, false);
        } catch (GeneralSecurityException e) {
            x_fixture.assertTrue("Caught unexpected: " + e, false);
        }
        return null;
    }

    /**
     * Try to authenticate the registered "test_user" user and a "bogus" user.
     */
    public void testLogin() {
        runLoginTest(os_test_user, os_test_user_password, this);
    }

    /**
     * Try to authenticate the registered "test_user" user and a "bogus" user
     * against the "clientlogin" configuration.
     */
    public void testClientModuleLogin() {
        Subject j_login = runLoginTest("littleware.security.clientlogin",
                os_test_user, os_test_user_password, this);
        // Verify that some of our security roles are satisfied
        Set<LittleRole> v_roles = j_login.getPrincipals(LittleRole.class);
        assertTrue("Got some LittleRole roles", !v_roles.isEmpty());
        assertTrue("Got a SessionHelper",
                !j_login.getPublicCredentials(SessionHelper.class).isEmpty());
    }

    /**
     * Reference a few services via the given SessionManager
     */
    public void testSessionSetup(SessionManager m_session) {
        try {
            assertTrue("Got a non-null SessionManager", null != m_session);
            SessionHelper m_helper = m_session.login(os_test_user, os_test_user_password,
                    "running testcase");
            AssetManager m_asset = m_helper.getService(ServiceType.ASSET_MANAGER);
            AssetSearchManager m_search = m_helper.getService(ServiceType.ASSET_SEARCH);
        } catch (Exception e) {
            olog_generic.log(Level.INFO, "Caught unexpected: " + e + ", " +
                    BaseException.getStackTrace(e));
            assertTrue("Caught unexpected: " + e, false);
        }
    }

    /**
     * Reference a few services via the constructor-supplied SessionManager
     */
    public void testSessionSetup() {
        testSessionSetup(om_session);
    }

    /**
     * Run testSessionSetup() against the session returned by
     *    SessionUtil.getSessionManager ( "localhost", SessionUtil.getRegistryPort () )
     */
    public void testSessionUtil() {
        try {
            // Force RMI
            SessionUtil    util = SessionUtil.get ();
            SessionManager m_session = util.getSessionManager("localhost",
                    util.getRegistryPort()
                    );
            testSessionSetup(m_session);
        } catch (Exception e) {
            olog_generic.log(Level.INFO, "Caught unexpected: " + e + ", " +
                    BaseException.getStackTrace(e));
            assertTrue("Caught unexpected: " + e, false);
        }
    }
}// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

