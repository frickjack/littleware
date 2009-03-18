package littleware.asset.test;

import com.google.inject.Provider;
import java.util.*;
import java.io.*;
import java.util.logging.Logger;
import java.util.logging.Level;


import littleware.base.*;
import littleware.asset.pickle.*;
import littleware.asset.*;
import littleware.test.LittleTest;


/**
* TestFixture instantiates different littleware.web.beans beans,
 * and exercises them a bit.
 */
public class PickleTester extends LittleTest {
	private static final Logger           olog_generic = Logger.getLogger ( PickleTester.class.getName() );
	
	private final Provider<? extends PickleMaker<Asset>> oprovidePickler;
	
	
	/**
	 * Constructor stashes PickleType to test against
	 */
	public PickleTester ( Provider<? extends PickleMaker<Asset>> providePickler ) {
		setName ( "testPickleTwice" );
		oprovidePickler = providePickler;
	}
	
	
	/**
	 * No seutp necessary
	 */
    @Override
	public void setUp () {
	}
	
	/** No tearDown necessary  */
    @Override
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
			oprovidePickler.get().pickle ( a_test, io_string );
			String s_first = io_string.toString ();
			olog_generic.log ( Level.INFO, "First pickle of test asset got: " + s_first );
			
			io_string.getBuffer ().setLength ( 0 );
			oprovidePickler.get().pickle ( a_test, io_string );
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

