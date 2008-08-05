package littleware.base.test;

import java.util.logging.Logger;
import java.util.logging.Level;

import junit.framework.*;

import littleware.base.*;

/**
 * Just little utility class that packages up a test suite
 * for the littleware.base package.
 */
public abstract class PackageTestSuite {

    /**
     * Setup a test suite to exercise this package -
     * junit.swingui.TestRunner looks for this.
     */
    public static Test suite() {
        TestSuite x_suite = new TestSuite("littleware.base.test.PackageTestSuite");
        Logger x_logger = Logger.getLogger("littleware.base.test");
        boolean b_run = true;

        if ( b_run ) {
            x_suite.addTest( new XmlResourceBundleTester( "testBasicXmlBundle" ) );
        }
        if (b_run) {
            int i_ageout_secs = 10;
            int i_maxsize = 100;
            Cache x_cache = new SimpleCache(i_ageout_secs, i_maxsize);
            x_suite.addTest(new CacheTester("testGeneric", x_cache, i_ageout_secs, i_maxsize));
            x_suite.addTest(new CacheTester("testAgeOut", x_cache, i_ageout_secs, i_maxsize));
            x_suite.addTest(new CacheTester("testSizeLimit", x_cache, i_ageout_secs, i_maxsize));
        }
        if (b_run) {
            x_suite.addTest(new UUIDFactoryTester("testFactory", UUIDFactory.getFactory()));
            x_suite.addTest(new DynamicEnumTester("testEnum"));
            x_suite.addTest(new XmlSpecialTester("testEncodeDecode"));
            x_suite.addTest(littleware.base.stat.test.PackageTestSuite.suite());
        }
        if (b_run) {
            ScriptRunner m_script = ScriptRunnerFactory.getFactory().create();

            x_suite.addTest(new ScriptTester("testCharUtil", m_script, "javascript"));
        }
        if (b_run) {
            x_suite.addTest(new SwingTester("testJTextAppender"));
            x_suite.addTest(new SwingTester("testJScriptRunner"));
            x_suite.addTest(new SwingTester("testListModelIterator"));
        }
        x_logger.log(Level.INFO, "PackageTestSuite.suite () returning ok ...");
        return x_suite;
    }

    /**
     * Run through the various lilttleware.sql test cases
     */
    public static void main(String[] v_args) {
        String[] v_launch_args = {"littleware.base.test.PackageTestSuite"};
        Logger x_logger = Logger.getLogger("littleware.base.test");

        x_logger.setLevel(Level.ALL);  // log everything during testing
        junit.swingui.TestRunner.main(v_launch_args);
    //junit.textui.TestRunner.main( v_launch_args );
    //junit.awtui.TestRunner.main( v_launch_args );
    }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

