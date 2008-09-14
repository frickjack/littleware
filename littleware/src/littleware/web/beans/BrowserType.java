package littleware.web.beans;

import java.util.regex.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Enumerate the different web browsers that we try to support
 */
public enum BrowserType {
	UNKNOWN,
	FIREFOX2,
	IE6,
	IE7,
	WEBKIT41x, // Safari - no SVG, weak XML
	WEBKIT42x; // Safari - SVG, better XML

	private static Logger olog_generic = Logger.getLogger ( "littleware.web.beans.BrowserType" );
	
	/** Key into HttpRequest HEADER attributes for the browser User-Agent info */
	public final static String OS_USER_AGENT = "User-Agent";
	
	/**
	 * Return the appropriate BrowserType member
	 * based on the given UserAgent string.
	 *
	 * @param s_user_agent User-Agent: HTTP header value
	 */
	public static BrowserType getBrowserFromAgent ( String s_user_agent ) {
		if ( null == s_user_agent ) {
			return UNKNOWN;
		}
		if ( s_user_agent.indexOf( "Firefox/" ) != -1 ) {
			return FIREFOX2;
		}
		{
			Matcher regex_match = Pattern.compile ( "AppleWebKit/(\\d+)\\D" ).matcher ( s_user_agent );
			if ( regex_match.find () ) {
				int i_version = Integer.parseInt ( regex_match.group ( 1 ) );
				if ( i_version >= 420 ) {
					return WEBKIT42x;
				}
				return WEBKIT41x;
			}
		}
        if ( s_user_agent.indexOf ( "MSIE 7" ) != -1 ) {
			return IE7;
		}
		if ( s_user_agent.indexOf ( "MSIE" ) != -1 ) {
			return IE6;
		}
		olog_generic.log ( Level.WARNING, "Unknown USER-AGENT: " + s_user_agent );
		return UNKNOWN;
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

