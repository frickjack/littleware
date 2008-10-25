package littleware.base;

/**
 * Convenience class efficiently wraps and array
 * with specified start/stop with a CharSequence interface
 */
public class ArrayCharSequence implements CharSequence, java.io.Serializable {
	char[]  ov_data = new char[0];
	int     oi_start = 0;
	int     oi_len = 0;
	
	/**
	 * Do nothing constructor intended for serialization only -
	 * sets up an empty CharSequence.
	 */
	public ArrayCharSequence () {}
	
	/**
	 * Constructor sets up a CharSequence accessing
	 * the given array from the given start point to the given length.
	 *
	 * @param v_char data
	 * @param i_start clamped to be between (0,v_char.length ()]
     * @param i_len clamped between (0, v_char.length() - i_start)
	 */
	public ArrayCharSequence ( char[] v_char, int i_start, int i_len ) {
		ov_data = v_char;
		if ( i_start < 0 ) {
			i_start = 0;
		} else if ( i_start > v_char.length ) {
			i_start = v_char.length - 1;
		}
		if ( i_len < 0 ) {
			i_len = 0;
		} else if ( i_len + i_start > v_char.length ) {
			i_len = v_char.length - i_start;
		}
		oi_start = i_start;
		oi_len = i_len;
	}
	
	public  char charAt( int i_index ) {
		return ov_data[ oi_start + i_index ];
	}
	
	public ArrayCharSequence subSequence( int i_start,
							  int i_end
							  ) {
		return new ArrayCharSequence ( ov_data, oi_start + i_start, i_end - i_start );
	}
	
	public int length () {
		return oi_len;
	}
	
	public String toString () {
		return new String ( ov_data, oi_start, oi_len );
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

