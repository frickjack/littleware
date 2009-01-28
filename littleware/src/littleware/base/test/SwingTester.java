package littleware.base.test;

import littleware.test.JLittleDialog;
import java.awt.*;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.logging.Level;


import javax.swing.*;

import junit.framework.*;

import littleware.base.*;
import littleware.base.swing.*;


/**
 * Test implementations of littleware.base.swing components
 */
public class SwingTester extends TestCase {
	private Logger  olog_generic = Logger.getLogger ( "littleware.base.test.SwingTester" );
	public static final String OS_NEWLINE = System.getProperty("line.separator");

	/**
	 * Do nothing constructor - calls through to super
	 */
	public SwingTester ( String s_name ) {
		super ( s_name );
	}
	
	/** Do nothing */
	public void setUp () {}
	
	/** Just flush the cache */
	public void tearDown () {}
	
	
	/**
	 * Little utility frickjack to maniipulate the JTextAppender
	 * while the user views the test
	 */
	private class TestAppender implements Runnable {
		private Appendable  oappend_test = null;
		private boolean   ob_shutdown = false;
		
		public TestAppender ( Appendable append_test ) {
			oappend_test = append_test;
		}
		
		public synchronized void shutdown () {
			if ( ! ob_shutdown ) {
				ob_shutdown = true;
				try {
					wait ();
				} catch ( InterruptedException e ) {
					olog_generic.log ( Level.INFO, "Caught waiting for shutdown: " + e );
				}
			}
		}
		
		
		public void run () {
			try {
				for ( int i=0; ! ob_shutdown; ++i ) {
					oappend_test.append ( "testline: " + i + ", sleep 1 second\n" );
					Thread.sleep ( 1000 );
				}
			} catch ( Exception e ) {
				olog_generic.log ( Level.INFO, "AppendThread caught: " + e );
			} finally {
				synchronized ( this ) {
					ob_shutdown = true;
					notifyAll ();
				}
			}
		}		
	}
	
	/**
	 * Test the JTextAppender
	 */
	public void testJTextAppender () {
		JTextAppender w_appender = new JTextAppender ();
		TestAppender  run_test = new TestAppender ( w_appender );
		try {
			Thread        thread_test = new Thread ( run_test );
			thread_test.start ();
			
			assertTrue ( "User says test went OK", 
						 JLittleDialog.showTestDialog ( w_appender,
							 "Please verify that text is scrolling correctly" +
							  OS_NEWLINE +
							 "Hit OK on success, CANCEL on failure"
										   )
						 );
		} finally {
			run_test.shutdown ();
		}
	}
	

	/**
	 * Test the JScriptRunner in conjunction with the JTextAppender
	 */
	public void testJScriptRunner () {
		JTextAppender w_appender = new JTextAppender ();
		JScriptRunner w_runner = new JScriptRunner ();
		JPanel        w_fullapp = JUtil.addAppenderToRunner ( w_appender, w_runner );		
		
		assertTrue ( "User says JScriptRunner test went OK", 
					 JLittleDialog.showTestDialog ( w_fullapp,
								  "Please verify that you can run a script in different languages" +
								  OS_NEWLINE +
								  "Hit OK on success, CANCEL on failure"
								  )
					 );
	}
    
    /** Test the ListModelIterator */
    public void testListModelIterator () {
        DefaultListModel lmodel_test = new DefaultListModel ();
        
        for ( int i=0; i < 10; ++i ) {
            lmodel_test.addElement ( i );
        }
        
        int i_count = 0;
        for ( Iterator<Object> r_i = new ListModelIterator( lmodel_test );
              r_i.hasNext ();
              ) {
            int i_check = ((Integer) r_i.next ()).intValue ();
            assertTrue ( "iterating ok: " + i_count + "?=" + i_check, i_count == i_check );
            ++i_count;
        }
        assertTrue ( "iterated to end of list: " + i_count, 10 == i_count );
    }
            
	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

