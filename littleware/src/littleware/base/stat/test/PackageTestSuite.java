package littleware.base.stat.test;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import junit.framework.*;

import littleware.base.stat.*;

/**
 * Setup test-suite for littleware.base.stat package
 */
public abstract class PackageTestSuite {
	
    /**
	 * Setup a test suite to exercise this package -
	 * junit.swingui.TestRunner looks for this.
	 */
    public static Test suite () {
        TestSuite x_suite = new TestSuite ( "littleware.base.stat.test.PackageTestSuite" );
		Logger    x_logger = Logger.getLogger ( "littleware.base.stat.test" );
		Sampler   x_sampler = new SimpleSampler ();
		
		x_suite.addTest ( new SamplerTester ( "testSampler", x_sampler ) );
		
		x_logger.log ( Level.INFO, "PackageTestSuite.suite () returning ok ..." );
        return x_suite;
    }
	
	
	/**
	 * Run through the various lilttleware.sql test cases
	 */
	public static void main ( String[] v_args ) {
		String[] v_launch_args = { "littleware.base.stat.test.PackageTestSuite" };
		Logger   x_logger = Logger.getLogger ( "littleware.base.stat.test" );
		
		x_logger.setLevel ( Level.ALL );  // log everything during testing
        junit.swingui.TestRunner.main( v_launch_args );
		//junit.textui.TestRunner.main( v_launch_args );
		//junit.awtui.TestRunner.main( v_launch_args );
	}
	

}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

