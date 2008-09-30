package littleware.test;

import java.util.logging.*;
import java.security.*;
import javax.security.auth.*;
import javax.security.auth.login.*;
import junit.framework.*;
import littleware.base.BaseException;
import littleware.base.swing.JPasswordDialog;
import littleware.security.auth.*;
import littleware.security.test.LoginTester;

/**
 * Test suite constructor that pulls together tests from
 * every littleware.*.test package.
 */
public class PackageTestSuite {

    /**
     * Setup a test suite to exercise the littleware subpackages -
     * sql, security, ...
     * junit.swingui.TestRunner looks for this.
     */
    public static Test suite() {
        TestSuite x_suite = new TestSuite("littleware.test.PackageTestSuite");
        Logger log_generic = Logger.getLogger("littleware.test");
        boolean b_run = true;

        log_generic.log(Level.INFO, "Trying to setup littleware.test test suite");
        try {
            if (b_run) {
                log_generic.log(Level.INFO, "Trying to setup littleware.base test suite");
                x_suite.addTest(littleware.base.test.PackageTestSuite.suite());
            }

            if (b_run) {
                log_generic.log(Level.INFO, "Trying to setup littleware.db test suite");
                x_suite.addTest(littleware.db.test.PackageTestSuite.suite());
            }

            if (b_run) {
                log_generic.log(Level.INFO, "Trying to setup littleware.asset test suite");
                x_suite.addTest(littleware.asset.test.PackageTestSuite.suite());
            }

            if (b_run) {
                log_generic.log(Level.INFO, "Trying to setup littleware.security test suite");
                x_suite.addTest(littleware.security.test.PackageTestSuite.suite());
            }

            if (b_run) {
                log_generic.log(Level.INFO, "Trying to setup littleware.web test suite");
                x_suite.addTest(littleware.web.test.PackageTestSuite.suite());
            }

            if (b_run) {
                log_generic.log(Level.INFO, "Trying to setup littleware.apps test suite");
                x_suite.addTest(littleware.apps.test.PackageTestSuite.suite());
            }
        } catch (RuntimeException e) {
            log_generic.log(Level.SEVERE, "Failed to setup test suite, caught: " + e + ", " + BaseException.getStackTrace(e));
            throw e;
        }
        log_generic.log(Level.INFO, "PackageTestSuite.suite () returning ok ...");
        return x_suite;
    }

    /**
     * Run through the various lilttleware test cases
     */
    public static void main(String[] v_args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                createAndShowGUI();
            }
        });
    }

    /** Private handler - runs on Swing dispatch thread */
    private static void createAndShowGUI() {
        Logger log_generic = Logger.getLogger("littleware");

        try {
            //log_generic.setLevel(Level.ALL); // log everything during testing
            log_generic.log(Level.INFO, "Setting up tests");
            log_generic.log ( Level.INFO, "Working directory: " +
                    new java.io.File( "." ).getAbsolutePath()
                    );

            // Must authenticate to run test case
            JPasswordDialog w_password = new JPasswordDialog( LoginTester.OS_TEST_USER, LoginTester.OS_TEST_USER_PASSWORD );


            if (!w_password.showDialog()) {
                log_generic.log(Level.INFO, "User selected cancel");
                return;
            }

            LoginContext login_context = new LoginContext("littleware.security.simplelogin", new SimpleNamePasswordCallbackHandler(w_password.getUserName(), w_password.getPassword()));
            login_context.login();

            Subject subject_user = login_context.getSubject();
            PrivilegedAction<Object> act_run = new PrivilegedAction<Object>() {

                private String[] ov_launch_args = {"-noloading", "littleware.test.PackageTestSuite"};

                public Object run() {
                    junit.swingui.TestRunner.main(ov_launch_args);
                    //junit.textui.TestRunner.main( v_launch_args );
                    return null;
                }
            };
            Subject.doAs(subject_user, act_run);
        } catch (Exception e) {
            log_generic.log(Level.WARNING, "Caught unexpected: " + e + ", " + littleware.base.BaseException.getStackTrace(e));
        }
    }
}
// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com
