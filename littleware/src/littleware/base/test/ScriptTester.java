package littleware.base.test;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.Writer;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.IOException;

import junit.framework.*;

import littleware.base.*;


/**
 * Test an implementation of the ScriptRunner interface.
 */
public class ScriptTester extends TestCase {
	private Logger        olog_generic = Logger.getLogger ( "littleware.base.test.ScriptTester" );
	private ScriptRunner  om_script = null;
	private String        os_javascript = null;
	
	/**
	 * Constructor just stuffs away the ScriptRunner to test
	 *
	 * @param s_name of test to run
	 * @param m_script to test against
	 * @param s_javascript language name to flip m_script into javascript mode
	 */
	public ScriptTester ( String s_name, ScriptRunner m_script, String s_javascript ) {
		super ( s_name );
		om_script = m_script;
		os_javascript = s_javascript;
	}
	
	/** Do nothing */
	public void setUp () {}
	
	/** Do nothing */
	public void tearDown () {}
	
	/**
	 * Developed a couple other classes to support the JScriptRunner -
	 * which we currently test in the SwingTester.
	 */
	public void testCharUtil () {
		String s_testcase = "0123456789abcdefghijklmnop";
		
		CharSequence seq_test = new ArrayCharSequence ( s_testcase.toCharArray (), 1, 20 );
		assertTrue ( "charAt(0) is 1", seq_test.charAt( 0 ) == '1' );
		CharSequence seq_sub = seq_test.subSequence ( 5, 11 );
		assertTrue ( "subsequence(5,11) expected 6789ab, got " + seq_sub,
					 "6789ab".contentEquals ( seq_sub )
					 );
		StringBuilder  s_builder = new StringBuilder ();
		Writer         write_appender = new AppenderWriter ( s_builder );
		
		try {
			write_appender.write ( s_testcase );
		} catch ( IOException e ) {
			olog_generic.log ( Level.INFO, "Caught unexpected: " + e + 
							   ", " + BaseException.getStackTrace ( e )
							   );
			assertTrue ( "Caught unexpected: " + e, false );
		} finally {
            try {
                write_appender.close ();
            } catch ( Exception e ) {}
        }
		
		String s_result = s_builder.toString ();
		assertTrue ( "WriteAppender got output: " + s_result,
					 s_testcase.equals ( s_result )
					 );
	}
	
	
	/**
	 * Run some simple javascript through the test object
	 * supplied to the constructor.
	 */
	public void testScriptRunner () {
		try {
			String       s_script = "bsf.stdout.print( \"OK\" );\n";
			StringWriter write_result = new StringWriter ();

			om_script.setLanguage ( os_javascript );
			om_script.setWriter ( new PrintWriter ( write_result ) );
			om_script.exec ( s_script );
			assertTrue ( "Script ran ok", write_result.toString ().equals ( "OK" ) );
		} catch ( ScriptException e ) {
			olog_generic.log ( Level.INFO, "Caught unexpected: " + e + ", " +
							   BaseException.getStackTrace ( e )
							   );
			assertTrue ( "Caught unexpected: " + e, false );
		}
	}
}
// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

