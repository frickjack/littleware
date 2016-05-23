package littleware.base;

import java.io.*;
import java.util.logging.Logger;
import java.util.logging.Level;


/** 
 * Little utility thread that reads from the given Reader,
 * and sends the result onto the given Writer.
 */
public class ReaderWriter implements Runnable {
	private static    Logger   olog_generic = Logger.getLogger ( "littleware.base.ReaderWriter" );
	private Reader    oread_in = null;
	private Writer    owrite_out = null;
	private Runnable  orun_post = null;
	
	/**
	 * Do nothing constructor - must subsequently
	 * call setReader and setWriter to setup the ReaderWriter
	 * before calling run().
	 */
	public ReaderWriter () {}
	
	/**
	 * Constructor stashes the reader source and the writer sink
	 * 
	 * @param read_in to read from until end of stream
	 * @param write_out to write to
	 * @param run_post to run() after end of stream reached or IOException - 
	 *                   may be null
	 */
	public ReaderWriter ( Reader read_in, 
						  Writer write_out,
						  Runnable run_post
						  ) {
		oread_in = read_in;
		owrite_out = write_out;
		orun_post = run_post;
	}
	
	public Reader getReader () { return oread_in; }
	public void   setReader ( Reader read_in ) { oread_in = read_in; }
	
	public Writer getWriter () { return owrite_out; }
	public void   setWriter ( Writer write_out ) { owrite_out = write_out; }
	
	public Runnable getPostCommand () { return orun_post; }
	public void     setPostCommand ( Runnable run_post ) { orun_post = run_post; }
	
	/**
	 * Just read till the end of stream gets reached
	 * or IOException thrown. 
	 *
	 * @throws NullPointerException if Reader or Writer are null
	 */
	public void run () {
		try {
			char[] v_buffer = new char[ 1024 ];
			
			for ( int i_len = oread_in.read ( v_buffer );
				  i_len >= 0;
				  i_len = oread_in.read ( v_buffer )
				  ) {
				if ( i_len > 0 ) {
					owrite_out.write ( v_buffer, 0, i_len );
				}
			}
			olog_generic.log ( Level.FINE, "end of stream reached" );
		} catch ( IOException e ) {
			olog_generic.log ( Level.INFO, "Caught unexepcted: " + e + ", " +
							   BaseException.getStackTrace ( e )
							   );
		} finally {
			try {
				owrite_out.flush ();
			} catch ( IOException e ) {
				olog_generic.log ( Level.INFO, "Final flush caught: " + e );
			}
			
			if ( null != orun_post ) {
				orun_post.run ();
			}
		}
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

