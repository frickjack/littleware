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

import java.security.PrivilegedAction;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import junit.framework.TestSuite;
import littleware.asset.AssetSearchManager;
import littleware.security.LittleUser;
import littleware.security.SecurityAssetType;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Funny singleton class facilitates launching junit test suites
 * against an in-JVM littleware server instance.
 * Usual setup is:
 *
 *      Implement an OSGi BundleActivator that extends
 *      TestSuite and GUICE injects its dependencies with
 *      the suite() method configured to watch for
 *      the GUICE allocated singleton instance,
 *      and bootstraps the littleware OSGi environment in main().
 *      Be sure to set the command line arguments to load
 *      a littleware server environment - littleware.home,
 *            logging.properties, security.policy, -Xmx
 *            properties on the command line:
 *              -Xms300m -Xmx300m -Dlittleware.properties=properties/littleware.properties -Djava.util.logging.config.file=properties/logging.properties -Dlittleware_jdbc.properties=properties/littleware_jdbc.properties -Djava.security.auth.login.config=properties/login.config -Djava.security.manager -Djava.security.policy=properties/littleware.policy -Dlittleware.install=.
 *
 *     Invoke createAndShowGUI from your application-specific
 *     ServerActivator
 *
 * It's a goofy setup, but works for now.  Take a look at
 * littleware.test.PackageTestSuite for an example.
 */
public abstract class ServerTestLauncher extends TestSuite implements BundleActivator {
    private static final Logger olog = Logger.getLogger( ServerTestLauncher.class.getName() );

    public static final String OS_TEST_USER = "littleware.test_user";
    public static final String OS_TEST_USER_PASSWORD = "test123";
    public static final String OS_TEST_GROUP = "group.littleware.test_user";

    private static ServerTestLauncher  osingleton = null;
    private final  AssetSearchManager  osearch;

    /**
     * Subtypes need to inject a couple things
     *
     * @param sSuiteName for TestSuite
     * @param search to lookup test user info with
     * @throws IllegalStateException if singleton already exists
     */
    protected ServerTestLauncher( String sSuiteName, AssetSearchManager search ) {
        super( sSuiteName );
        osearch = search;
    }

    /**
     * Return the already GUICE injected singleton, or throw IllegalStateException
     * if singleton does not exist.  The internal singleton gets set by the
     * createAndShowGUI() method which points the GUI testRunner at this.getClass().getName()
     * which in turn invokes this.getClass().suite() via reflection.
     */
    public static Test suite () {
        if ( null == osingleton ) {
            throw new IllegalStateException( "Singleton not initialized via OSGi startup" );
        }
        return osingleton;
    }


    /**
     * Launches junit swing test runner on the given TestSuite class
     * on the SwingDispatch thread running as OS_TEST_USER.
     * Sets osingleton to this.
     *
     * @param classTest to run
     */
    protected void createAndShowGUI() {
        // hacky global singleton to marry OSGi bootstrap with junit TestRunner bootstrap
        if ( null != osingleton ) {
            throw new IllegalStateException( "Singleton already allocated" );
        }
        osingleton = this;

        if ( ! SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater( new Runnable() {
                @Override
                public void run() {
                    createAndShowGUI();
                }
            });
            return;
        }
        try {
            //olog.setLevel(Level.ALL); // log everything during testing
            olog.log(Level.INFO, "Setting up tests");
            olog.log ( Level.INFO, "Working directory: " +
                    new java.io.File( "." ).getAbsolutePath()
                    );

            // Create a bogus LittleUser
            LittleUser userTest = null; //osearch.getByName(OS_TEST_USER, SecurityAssetType.USER );
            //userTest.setName( "littleware.test_user" );
            //userTest.setObjectId( UUIDFactory.parseUUID( "7AC5D21049254265B224B7512EFCF0D1"));
                    //
            final Subject subject = null; //new Subject();
            //subject.getPrincipals().add( userTest );

            final String[] ov_launch_args = {"-noloading", this.getClass().getName() };

            PrivilegedAction<Object> action = new PrivilegedAction<Object>() {

                @Override
                public Object run() {
                    junit.swingui.TestRunner.main(ov_launch_args);
                    //junit.textui.TestRunner.main( v_launch_args );
                    return null;
                }
            };
            // Hard code for simple test case
            action.run();
            //Subject.doAs(subject, action);
        } catch (Exception e) {
            olog.log(Level.SEVERE, "Caught unexpected: " + e + ", " + littleware.base.BaseException.getStackTrace(e));
        }
    }

    
    @Override
    public void start(BundleContext ctx) throws Exception {
        SwingUtilities.invokeLater( new Runnable() {
            @Override
            public void run () {
                createAndShowGUI();
            }
        });
    }

    /** NOOP default implementation */
    @Override
    public void stop(BundleContext ctx) throws Exception {
    }
}
