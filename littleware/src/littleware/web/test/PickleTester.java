package littleware.web.test;

import java.util.*;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.logging.Logger;
import java.util.logging.Level;

import junit.framework.*;

import littleware.base.*;
import littleware.web.*;
import littleware.web.pickle.*;
import littleware.asset.*;


/**
* TestFixture instantiates different littleware.web.beans beans,
 * and exercises them a bit.
 */
public class PickleTester extends TestCase {
	private static Logger           olog_generic = Logger.getLogger ( "littelware.web.test.PickleTester" );
	
	private PickleType              on_pickle = null;
	
	
	/**
	 * Constructor stashes PickleType to test against
	 */
	public PickleTester ( String s_testname, PickleType n_pickle ) {
		super ( s_testname );
		on_pickle = n_pickle;
	}
	
	
	/**
	 * No seutp necessary
	 */
	public void setUp () {
	}
	
	/** No tearDown necessary  */
	public void tearDown () {
	}
	
	/**
	 * Stupid little test - check whether pickling an AssetType.GENERIC
	 * asset twice yields the same result both times.
	 * Not all PickleMaker may require that.
	 */
	public void testPickleTwice () {
		try {
			Asset  a_test = AssetType.GENERIC.create ();
			a_test.setName ( "bogus_pickletest_asset" );
			a_test.setObjectId ( UUID.randomUUID () );
			a_test.setHomeId ( UUID.randomUUID () );
			a_test.setToId ( UUID.randomUUID () );
			a_test.setComment ( "Test comment" );
			a_test.setCreatorId ( UUID.randomUUID () );
			a_test.setLastUpdaterId ( UUID.randomUUID () );
			a_test.setLastUpdate ( "Bla bla" );
			
			StringWriter io_string = new StringWriter ();
			PickleMaker<Asset> pickle_handler = on_pickle.createPickleMaker ( a_test.getAssetType () );
			pickle_handler.pickle ( a_test, io_string );
			String s_first = io_string.toString ();
			olog_generic.log ( Level.INFO, "First pickle of test asset got: " + s_first );
			
			io_string.getBuffer ().setLength ( 0 );
			pickle_handler.pickle ( a_test, io_string );
			String s_second = io_string.toString ();
			assertTrue ( "Pickle twice, got same result", s_first.equals ( s_second ) );
		} catch ( Exception e ) {
			olog_generic.log ( Level.INFO, "Caught unexected: " + e + ", " +
							   BaseException.getStackTrace ( e )
							   );
			assertTrue ( "Caught unexpected: " + e, false );
		}
	}
	
}
// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

