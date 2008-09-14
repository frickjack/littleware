package littleware.asset.test;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import java.io.*;

import junit.framework.*;

import littleware.asset.*;
import littleware.asset.xml.*;
import littleware.base.*;


/**
 * Tester for the SimpleXmlDataAsset Asset super class handling
 * of XML-based getData/setData via annotations.
 */
public class XmlAssetTester extends TestCase {
	private static Logger olog_generic = Logger.getLogger ( "littleware.asset.test.XmlAssetTester" );
	
	
	/**
     * Do nothing constructor - just pass the test-name through to super.
	 */
	public XmlAssetTester ( String s_test_name ) {
		super( s_test_name );
	}
	
	/** Do nothing */
	public void setUp () {}
	
	/** Do nothing */
	public void tearDown () {}
	
	/** Little enum-type to support TestAsset below */
	public enum TestType {
		OK, FRICK;
		
		public static TestType parse ( String s_item ) throws ParseException {
			if ( s_item.equals ( OK.toString () ) ) {
				return OK;
			}
			if ( s_item.equals ( FRICK.toString () ) ) {
				return FRICK;
			}
			throw new ParseException ( "Invalid string: " + s_item );
		}
	}

	/**
	 * Little test class 
	 */
	public static class TestAsset extends SimpleXmlDataAsset {
		private String   os_foo = "foo";
		private String   os_bla = "bla";
		private int      oi_100 = 100;
		private TestType on_type = TestType.OK;
		
		
		/** Custom XML setter for setNum100 */
		public static class Num100Setter extends SimpleXmlDataSetter {
			public void setData ( Object x_target, String s_data ) {
				TestAsset a_test = (TestAsset) x_target;
				a_test.setNum100 ( Integer.parseInt ( s_data ) );
			}
		}
		
		/** Custom XML setter for setNum100 */
		public static class TestTypeSetter extends SimpleXmlDataSetter {
			public void setData ( Object x_target, String s_data ) throws ParseException {
				TestAsset a_test = (TestAsset) x_target;
				a_test.setTestType ( TestType.parse ( s_data ) );
			}
		}
			
	 
		/** Namespace for getData/setData XML data */
		public final static String OS_NAMESPACE = 			
			"http://www.littleware.com/xml/namespace/2006/testxml";

		/** 
		 * Constructor just assigns AssetType.GENERIC,
		 * an object-id, and a name.  Passes namespace and prefix
		 * through to super.
		 */
		public TestAsset () {
			super ( OS_NAMESPACE, "txml", "test_data" );
			setName ( "testcase" );
			setObjectId ( UUID.randomUUID () );
			setAssetType ( AssetType.GENERIC );
		}
		
		@XmlGetter( element="bla" )
		public String getBla () { return os_bla; }
		
		@XmlSetter( element="bla" )
		public void setBla ( String s_bla ) { os_bla = s_bla; }
		
		@XmlGetter( element="foo" )
		public String getFoo () { return os_foo; }
		
		@XmlSetter( element="foo" )
		public void setFoo ( String s_foo ) { os_foo = s_foo; }
		
		@XmlGetter( element="", attribute="num100" )
		public int getNum100 () { return oi_100; }
		
		@XmlSetter( element="", attribute="num100", setter=Num100Setter.class )
		public void setNum100 ( int i_100 ) { oi_100 = i_100; }
		
		@XmlGetter( element="ttype" )
		public TestType getTestType () { return on_type; }
		
		@XmlSetter( element="ttype", setter=TestTypeSetter.class )
		public void setTestType ( TestType n_type ) { on_type = n_type; }
		
		/** Clear all the getData() data elements to null to support testing */
		public void clearData () {
			os_bla = null;
			os_foo = null;
			oi_100 = 0;
			on_type = null;
		}
	}
	
	/**
	 * Run the TestAsset SimpleXmlDataAsset test subtype through some getData/setData
	 * tests and XML verification.
	 */
	public void testXmlAsset () {
		try {
			Asset  a_xml = new TestAsset ();
			String s_xml = a_xml.getData ();
			
			olog_generic.log ( Level.INFO, "TestAsset getData got: " + s_xml );
			
			// Verify that the freakin data is properly formed XML
			{
				SAXParserFactory factory = SAXParserFactory.newInstance();
				factory.setNamespaceAware ( true );
				SAXParser sax_parser = factory.newSAXParser();
				DefaultHandler  sax_handler = new DefaultHandler ();
				
				sax_parser.parse( new InputSource ( new StringReader ( s_xml ) ), 
								  sax_handler 
								  );
				// If no exception thrown, then the XML is properly formed
			}
			
			((TestAsset) a_xml).clearData ();
			a_xml.setData ( s_xml );
			String s_xml2 = a_xml.getData ();
			
			olog_generic.log ( Level.INFO, "Data reset getData got: " + s_xml2 );
			assertTrue ( "Data reset matches original data", s_xml2.equals ( s_xml ) );
		} catch ( Exception e ) {
			olog_generic.log ( Level.WARNING, "Caught: " + e + 
							   ", " + BaseException.getStackTrace ( e ) );
			assertTrue ( "Caught: " + e, false );
		}			
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

