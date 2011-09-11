/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.test;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.logging.*;
import junit.framework.*;
import littleware.bootstrap.AppBootstrap;

/**
 * Test suite constructor that pulls together tests from
 * every littleware.*.test package.
 */
public class PackageTestSuite extends TestSuite {

    private static final Logger log = Logger.getLogger(PackageTestSuite.class.getName());

    @Inject
    public PackageTestSuite(
            JenkinsTestSuite jenkinsSuite,
            littleware.apps.swingbase.test.PackageTestSuite swingBaseSuite) {
        super(PackageTestSuite.class.getName());
        // disable server tests
        final boolean bRun = true;

        log.log(Level.INFO, "Trying to setup littleware.test test suite");
        try {
            this.addTest( jenkinsSuite );

            if (bRun) {
                log.log(Level.INFO, "Trying to setup littleware.apps.swingbase test suite");
                this.addTest(swingBaseSuite);
            }

        } catch (RuntimeException ex) {
            log.log(Level.SEVERE, "Failed to setup test suite", ex);
            throw ex;
        }
        log.log(Level.INFO, "PackageTestSuite.suite () returning ok ...");
    }

    /**
     * Just call through to ServerTestLauncher.suite() - should only
     * invoke when this is the master SeverTestLauncher TestSuite.
     */
    public static Test suite() {
        log.log(Level.WARNING, "Guice 2.0 has an AOP bug that may throw an exception booting in test-runner class-loader");
        try {
            return (new TestFactory()).build(
                    littleware.apps.swingbase.test.PackageTestSuite.registerSwingBase(
                    AppBootstrap.appProvider.get().profile(AppBootstrap.AppProfile.SwingApp)).build(),
                    PackageTestSuite.class);
        } catch (RuntimeException ex) {
            log.log(Level.SEVERE, "Test setup failed", ex);
            throw ex;
        }
    }
}
