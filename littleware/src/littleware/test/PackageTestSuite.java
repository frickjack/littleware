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

import com.google.inject.Inject;
import java.util.logging.*;
import junit.framework.*;
//import littleware.apps.client.ClientBootstrap;
import littleware.asset.AssetSearchManager;
import littleware.base.BaseException;
import littleware.security.auth.server.ServerBootstrap;

/**
 * Test suite constructor that pulls together tests from
 * every littleware.*.test package.
 */
public class PackageTestSuite extends TestSuite {

    private static final Logger log = Logger.getLogger(PackageTestSuite.class.getName());

    @Inject
    public PackageTestSuite(
            littleware.base.test.PackageTestSuite suite_base,
            littleware.asset.test.PackageTestSuite suite_asset,
            littleware.security.test.PackageTestSuite suite_security,
            AssetSearchManager search) {
        super(PackageTestSuite.class.getName());
        // disable server tests
        final boolean bRun = true;

        log.log(Level.INFO, "Trying to setup littleware.test test suite");
        try {
            if (bRun) {
                log.log(Level.INFO, "Trying to setup littleware.base test suite");
                this.addTest(suite_base);
            }

            if (bRun) {
                log.log(Level.INFO, "Trying to setup littleware.db test suite");
                log.log(Level.INFO, "Test disabled ... does not apply when running with JPA");
                //this.addTest( suite_db );
            }

            if (bRun) {
                log.log(Level.INFO, "Trying to setup littleware.asset test suite");
                this.addTest(suite_asset);
            }

            if (bRun) {
                log.log(Level.INFO, "Trying to setup littleware.security test suite");
                this.addTest(suite_security);
            }

        } catch (RuntimeException e) {
            log.log(Level.SEVERE, "Failed to setup test suite, caught: " + e + ", " + BaseException.getStackTrace(e));
            throw e;
        }
        log.log(Level.INFO, "PackageTestSuite.suite () returning ok ...");
    }
    private static TestSuite suite = null;

    /**
     * Just call through to ServerTestLauncher.suite() - should only
     * invoke when this is the master SeverTestLauncher TestSuite.
     */
    public static Test suite() {
        if (null != suite) {
            return suite;
        } else {
            log.log( Level.WARNING, "Guice 2.0 has an AOP bug that may throw an exception booting in test-runner class-loader" );
            try {
                return (new TestFactory()).build(new ServerBootstrap(true), PackageTestSuite.class);
            } catch (RuntimeException ex) {
                log.log(Level.SEVERE, "Test setup failed", ex);
                throw ex;
            }
        }
    }

    /**
     * Boot the littleware server OSGi environment,
     * and register this master test suite as a BundleActivator.
     */
    public static void main(String[] args) {
        String[] launchArgs = {"-noloading", "littleware.test.PackageTestSuite"};
        suite = (new TestFactory()).build(new ServerBootstrap(true), PackageTestSuite.class);
        
        junit.swingui.TestRunner.main(launchArgs);
        //junit.textui.TestRunner.main( v_launch_args );
    }
}
