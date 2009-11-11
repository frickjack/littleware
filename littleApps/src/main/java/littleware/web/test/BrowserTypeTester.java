package littleware.web.test;

import java.util.*;
import java.sql.*;
import java.security.*;
import java.security.acl.*;
import javax.security.auth.login.*;
import javax.mail.*;

import java.util.logging.Logger;
import java.util.logging.Level;

import junit.framework.*;

import littleware.base.*;
import littleware.web.*;
import littleware.web.beans.*;


/**
 * TestFixture instantiates different littleware.web.beans beans,
 * and exercises them a bit.
 */
public class BrowserTypeTester extends TestCase {
	private static Logger           olog_generic = Logger.getLogger ( "littelware.web.test.BrowserTypeTester" );
	
	
	/**
	* Do nothing constructor
	 */
	public BrowserTypeTester ( String s_name ) {
		super ( s_name );
	}
	
	
	/**
	 * No setup necessary
	 */
	public void setUp () {
	}
	
	/** No tearDown necessary */
	public void tearDown () {
	}
	
	private static Map<BrowserType,String>  ov_browser_agent = new EnumMap<BrowserType,String>( BrowserType.class );
	
	static {
		ov_browser_agent.put ( BrowserType.WEBKIT41x,
							   "Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/418.9.1 (KHTML, like Gecko) Safari/419.3"
							   );
		ov_browser_agent.put ( BrowserType.WEBKIT42x,
							   "Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/420+ (KHTML, like Gecko) Safari/419.3"
							   );		
		
		ov_browser_agent.put ( BrowserType.IE7,
								"Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322)"
							   );
		ov_browser_agent.put ( BrowserType.IE6,
								"Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.1.4322)"
							   );
		ov_browser_agent.put ( BrowserType.FIREFOX2,
								"Mozilla/5.0 (Macintosh; U; PPC Mac OS X Mach-O; en-US; rv:1.8.1) Gecko/20061010 Firefox/2.0"
							   );
	}
	
	/**
	 * Test BrowserType.getBrowserFromUserAgent ()
	 */
	public void testUserAgent () {
		for ( Map.Entry<BrowserType,String> map_entry : ov_browser_agent.entrySet () ) {
			olog_generic.log ( Level.INFO, "Testing agent: " + map_entry.getValue () );
			BrowserType  n_result = BrowserType.getBrowserFromAgent ( map_entry.getValue () );
			assertTrue ( "Agent should be: " + map_entry.getKey () + ", got " + n_result + 
						 " from: " + map_entry.getValue (),
						 map_entry.getKey ().equals ( n_result )
						 );
		}
	}
	
}
// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

