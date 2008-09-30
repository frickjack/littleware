package littleware.base.test;

import java.util.logging.Logger;
import java.util.logging.Level;

import junit.framework.*;

import littleware.base.*;


/**
 * Run the XmlSpecial encoder/decoder through some paces.
 */
public class XmlSpecialTester extends TestCase {
	private Logger  olog_generic = Logger.getLogger ( "littleware.base.test.XmlSpecialTester" );
	
	/**
	 * Constructor just call through to super class
	 */
	public XmlSpecialTester ( String s_name ) {
		super ( s_name );
	}
	
	/** Do nothing */
	public void setUp () {}
	
	/** Do nothing */
	public void tearDown () {}
	
	/**
	 * Run some generic encode/decode tests
	 */
	public void testEncodeDecode () {
		String  s_all_raw = null;
		String  s_all_encoded = null;
		
		{
			StringBuilder s_build_raw = new StringBuilder ();
			StringBuilder s_build_encoded = new StringBuilder ();
			
			
			for ( XmlSpecial n_special : XmlSpecial.values () ) {
				String s_decode = XmlSpecial.decode ( n_special.getEncoding () );
				olog_generic.log ( Level.INFO, "decode( " + n_special.getEncoding () + ") -> " + 
								   s_decode + ", expecting: " + n_special.getChar ()
								   );
				assertTrue ( s_decode.equals ( Character.toString ( n_special.getChar () ) ) );
				assertTrue ( n_special.getEncoding ().equals( XmlSpecial.encode ( s_decode ) )
						  );
				s_build_raw.append ( n_special.getChar () );
				s_build_encoded.append ( n_special.getEncoding () );
			}
			s_all_raw = s_build_raw.toString ();
			s_all_encoded = s_build_encoded.toString ();
		}
		assertTrue ( XmlSpecial.encode ( s_all_raw ).equals ( s_all_encoded ) );
		String s_decode = XmlSpecial.decode ( s_all_encoded );
		olog_generic.log ( Level.INFO, "decode( \"" + s_all_encoded + "\" ) -> \"" + s_decode + "\", expected: " + s_all_raw );
		assertTrue ( s_decode.equals ( s_all_raw ) );
		
		// Test with non-special chars
		String s_test_raw = "bla " + s_all_raw + " bla " + s_all_raw + "bla ";
		String s_test_encoded = "bla " + s_all_encoded + " bla " + s_all_encoded + "bla ";
		
		assertTrue ( XmlSpecial.encode ( s_test_raw ).equals ( s_test_encoded ) );
		assertTrue ( XmlSpecial.decode ( s_test_encoded ).equals ( s_test_raw ) );
	}
		
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

