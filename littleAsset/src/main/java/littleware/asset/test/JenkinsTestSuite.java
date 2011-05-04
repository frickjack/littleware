/*
 * Copyright 2011 http://code.google.com/p/littleware/
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.test;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.logging.Logger;
import java.util.logging.Level;

import junit.framework.*;
import littleware.asset.GenericAsset;

import littleware.asset.pickle.HumanPicklerProvider;
import littleware.asset.pickle.XmlPicklerProvider;


/**
 * Test suite for littleware.asset package
 */
public class JenkinsTestSuite extends TestSuite {
    private static final Logger log = Logger.getLogger(JenkinsTestSuite.class.getName());

    /**
     * Inject dependencies necessary to setup the TestSuite
     */
    @Inject
    public JenkinsTestSuite(
            HumanPicklerProvider  humanPickler,
            XmlPicklerProvider    xmlPickler,
            Provider<GenericAsset.GenericBuilder> genericProvider,
            Provider<AssetTypeTester> provideTypeTester
            ) {
        super(JenkinsTestSuite.class.getName());
        boolean runTest = true;

        if( runTest ) {
            this.addTest( provideTypeTester.get() );
        }
        if (runTest) {
            this.addTest(new PickleTester( humanPickler, genericProvider ));
            this.addTest(new PickleTester( xmlPickler, genericProvider ) );
        }

        log.log(Level.INFO, "PackageTestSuite.suite () returning ok ...");
    }
}

