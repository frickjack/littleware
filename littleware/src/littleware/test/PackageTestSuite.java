/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.test;

import com.google.inject.Inject;
import java.util.logging.*;
import java.security.*;
import javax.security.auth.*;
import javax.security.auth.login.*;
import javax.swing.SwingUtilities;
import junit.framework.*;
import littleware.base.BaseException;
import littleware.base.swing.JPasswordDialog;
import littleware.security.auth.*;
import littleware.security.auth.server.ServerBootstrap;
import littleware.security.test.LoginTester;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Test suite constructor that pulls together tests from
 * every littleware.*.test package.
 */
public class PackageTestSuite extends TestSuite implements BundleActivator {
    private static final Logger olog = Logger.getLogger( PackageTestSuite.class.getName() );

    @Inject
    public PackageTestSuite(
            littleware.base.test.PackageTestSuite suite_base,
            littleware.db.test.PackageTestSuite suite_db,
            littleware.asset.test.PackageTestSuite suite_asset,
            littleware.security.test.PackageTestSuite suite_security,
            littleware.web.test.PackageTestSuite suite_web,
            littleware.apps.test.PackageTestSuite suite_apps
            ) {
        super( PackageTestSuite.class.getName() );
        boolean b_run = true;

        olog.log(Level.INFO, "Trying to setup littleware.test test suite");
        try {
            if (b_run) {
                olog.log(Level.INFO, "Trying to setup littleware.base test suite");
                this.addTest( suite_base );
            }

            if (b_run) {
                olog.log(Level.INFO, "Trying to setup littleware.db test suite");
                this.addTest( suite_db );
            }

            if (b_run) {
                olog.log(Level.INFO, "Trying to setup littleware.asset test suite");
                this.addTest( suite_asset );
            }

            if (b_run) {
                olog.log(Level.INFO, "Trying to setup littleware.security test suite");
                this.addTest( suite_security );
            }

            if (b_run) {
                olog.log(Level.INFO, "Trying to setup littleware.web test suite");
                this.addTest( suite_web );
            }

            if (b_run) {
                olog.log(Level.INFO, "Trying to setup littleware.apps test suite");
                this.addTest( suite_apps );
            }
        } catch (RuntimeException e) {
            olog.log(Level.SEVERE, "Failed to setup test suite, caught: " + e + ", " + BaseException.getStackTrace(e));
            throw e;
        }
        olog.log(Level.INFO, "PackageTestSuite.suite () returning ok ...");
    }

    /**
     * Boot the littleware server OSGi environment,
     * and register this master test suite as a BundleActivator.
     */
    public static void main(String[] v_args) {
        ServerBootstrap boot = new ServerBootstrap();
        boot.getOSGiActivator().add( PackageTestSuite.class );
        boot.bootstrap();
    }

    /** Private handler - runs on Swing dispatch thread */
    private void createAndShowGUI() {
        try {
            //olog.setLevel(Level.ALL); // log everything during testing
            olog.log(Level.INFO, "Setting up tests");
            olog.log ( Level.INFO, "Working directory: " +
                    new java.io.File( "." ).getAbsolutePath()
                    );

            // Must authenticate to run test case
            JPasswordDialog w_password = new JPasswordDialog( LoginTester.OS_TEST_USER, LoginTester.OS_TEST_USER_PASSWORD );


            if (!w_password.showDialog()) {
                olog.log(Level.INFO, "User selected cancel");
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
            olog.log(Level.WARNING, "Caught unexpected: " + e + ", " + littleware.base.BaseException.getStackTrace(e));
        }
    }

    public void start(BundleContext ctx) throws Exception {
        SwingUtilities.invokeLater( new Runnable() {
            public void run () {
                createAndShowGUI();
            }
        });
    }

    public void stop(BundleContext ctx) throws Exception {
    }
}
