package littleware.db.test;

import java.util.logging.Logger;
import java.util.logging.Level;

import junit.framework.*;

import littleware.db.*;

/**
 * Just little utility class that packages up a test suite
 * for the littleware.db package.
 */
public abstract class PackageTestSuite {

    /**
     * Setup a test suite to exercise this package -
     * junit.swingui.TestRunner looks for this.
     */
    public static Test suite() {
        TestSuite x_suite = new TestSuite("littleware.db.test.PackageTestSuite");
        Logger x_logger = Logger.getLogger("littleware.db.test");
        x_logger.log(Level.INFO, "Trying to setup littleware.db test suite");

        try {
            ConnectionFactory x_factory = new ProxoolConnectionFactory("org.postgresql.Driver",
                    "jdbc:postgresql:demo://localhost:5432",
                    "demo_user",
                    "demo_user_password",
                    "demo_pool",
                    20);
            x_suite.addTest(new ConnectionFactoryTester("testQuery", x_factory, "SELECT 'Hello'"));
        } catch (LittleSqlException e) {
            x_logger.log(Level.SEVERE, "Failed setting up postgres test proxool connection pool, caught: " + e);
        }
        x_logger.log(Level.INFO, "PackageTestSuite.suite () returning ok ...");
        return x_suite;
    }

    /**
     * Run through the various lilttleware.sql test cases
     */
    public static void main(String[] v_args) {
        String[] v_launch_args = {"littleware.db.test.PackageTestSuite"};
        Logger x_logger = Logger.getLogger("littleware.db.test");

        x_logger.setLevel(Level.ALL);  // log everything during testing
        junit.swingui.TestRunner.main(v_launch_args);
    //junit.textui.TestRunner.main( v_launch_args );
    //junit.awtui.TestRunner.main( v_launch_args );
    }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

