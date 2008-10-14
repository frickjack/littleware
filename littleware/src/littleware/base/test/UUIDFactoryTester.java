package littleware.base.test;

import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;

import junit.framework.TestCase;

import littleware.base.*;

/**
 * Just run UUIDFactory implementations through a simple test
 */
public class UUIDFactoryTester extends TestCase {
	private static Logger olog_generic = Logger.getLogger ( "littleware.base.test.UUIDFactoryTester" );
	private Factory<UUID> ofactory_uuid = null;
	
	/**
	 * Constructor stashes name of test to run,
	 * and UUIDFactory to run test against
	 */
	public UUIDFactoryTester ( String s_name, Factory<UUID> factory_uuid ) {
		super( s_name );
		ofactory_uuid = factory_uuid;
	}
	
	/** No setup necessary */
	public void setUp () {}
	/** No tearDown necessary */
	public void tearDown () {}
	
	/**
	 * Just create a couple UUID's, then go back and
	 * forth to the string representation
	 */
	public void testFactory () {
		try {
			UUID u_test1 = ofactory_uuid.create ();
			UUID u_test2 = ofactory_uuid.create ();
			
			assertTrue ( "Got 2 different id's", ! u_test1.equals ( u_test2 ) );
			UUID u_from_string = UUIDFactory.parseUUID ( u_test1.toString () );
			assertTrue ( "Able to parse " + u_test1, u_from_string.equals ( u_test1 ) );
			u_from_string = UUIDFactory.parseUUID ( u_test1.toString ().replaceAll ( "-", "" ) );
			assertTrue ( "Able to parse with no dashes: " + u_test1,
						 u_from_string.equals ( u_test1 )
						 );
		} catch ( Exception e ) {
			olog_generic.log ( Level.WARNING, "Caught unexpected: " + e );
			assertTrue ( "Caught unexpected: " + e, false );
		}
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

