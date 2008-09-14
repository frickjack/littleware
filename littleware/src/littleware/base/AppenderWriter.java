package littleware.base;

import java.io.Writer;
import java.io.IOException;

/**
 * Little utility to facilitate abstracting the JTextAppender 
 * and other Appendables via a Writer interface.
 */
public class AppenderWriter extends Writer {
	private Appendable  oappend_wrap = null;
	
	/**
     * Constructor takes and appender to wrap with
	 * a Writer
	 */
	public AppenderWriter ( Appendable append_wrap ) {
		oappend_wrap = append_wrap;
	}
	
	/** Do nothing */
	public void flush () {}
	
	/** Do nothing */
	public void close () {}
	
	/** Append to the internal appender */
	public void write ( char[] v_char, int i_start, int i_len ) throws IOException {
		oappend_wrap.append ( new ArrayCharSequence ( v_char, i_start, i_len ) );
	}
}


// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

