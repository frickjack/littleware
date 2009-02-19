/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.test;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import java.io.IOException;
import java.util.logging.*;
import java.security.*;
import javax.security.auth.*;
import javax.security.auth.login.*;
import javax.swing.SwingUtilities;
import junit.framework.*;
import littleware.base.BaseException;
import littleware.base.PropertiesGuice;
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
    private static PackageTestSuite  osingleton = null;

    @Inject
    public PackageTestSuite(
            littleware.base.test.PackageTestSuite suite_base,
            littleware.db.test.PackageTestSuite suite_db,
            littleware.asset.test.PackageTestSuite suite_asset,
            littleware.security.test.PackageTestSuite suite_security,
            littleware.security.auth.server.SimpleDbLoginConfiguration config
            ) {
        super( PackageTestSuite.class.getName() );
        // disable server tests
        final boolean b_run = true;

        // hacky global singleton to marry OSGi bootstrap with junit TestRunner bootstrap
        if ( null != osingleton ) {
            throw new IllegalStateException( "Singleton already allocated" );
        }
        osingleton = this;
        
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

            if ( b_run ) {
                olog.log(Level.INFO, "Trying to setup littleware.asset test suite");
                this.addTest( suite_asset );
            }

            if (b_run) {
                olog.log(Level.INFO, "Trying to setup littleware.security test suite");
                this.addTest( suite_security );
            }

        } catch (RuntimeException e) {
            olog.log(Level.SEVERE, "Failed to setup test suite, caught: " + e + ", " + BaseException.getStackTrace(e));
            throw e;
        }
        olog.log(Level.INFO, "PackageTestSuite.suite () returning ok ...");
    }

    public static Test suite () {
        if ( null == osingleton ) {
            throw new IllegalStateException( "Singleton not initialized via OSGi startup" );
        }
        return osingleton;
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

    /**
     * Internal utility runs after OSGi server side bootstrap -
     * registers client-side test cases via a separate
     * Guice injection process.
     */
    private void addClientTests () throws IOException {
        Injector     injector = Guice.createInjector( new Module[] {
                            new littleware.apps.swingclient.StandardSwingGuice(),
                            new littleware.apps.client.StandardClientGuice(),
                            new littleware.apps.misc.StandardMiscGuice(),
                            new littleware.security.auth.ClientServiceGuice(),
                            new PropertiesGuice( littleware.base.PropertiesLoader.get().loadProperties() )
                        }
            );

        final boolean b_run = true;

        if ( false ) {
            // TODO - guice enable JSF beans 
            // TODO - move web and apps test cases over to ClientTestSuite
            olog.log(Level.INFO, "Trying to setup littleware.web test suite");
            this.addTest( injector.getInstance( littleware.web.test.PackageTestSuite.class ) );
        }

        if (b_run) {
            // TODO - workout OSGi bootstrap with server framework
            olog.log(Level.INFO, "Trying to setup littleware.apps test suite");
            this.addTest( injector.getInstance( littleware.apps.test.PackageTestSuite.class ) );
        }

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
        addClientTests();
        SwingUtilities.invokeLater( new Runnable() {
            public void run () {
                createAndShowGUI();
            }
        });
    }

    public void stop(BundleContext ctx) throws Exception {
    }
}
