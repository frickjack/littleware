package littleware.base;

import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.ResourceBundle;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;


/**
 * Set of XML special characters that must be
 * encoded/decoded
 */
public enum XmlSpecial {
	lt {
		/** @return "&lt;" */
		public String getEncoding () { return "&lt;"; }
		/** @return '<' */
		public char getChar () { return '<'; }
	},
	gt {
		/** @return "&gt;" */
		public String getEncoding () { return "&gt;"; }
		/** @return '>' */
		public char getChar () { return '>'; }
	},
	apos {
		/** @return "&apos;" */
		public String getEncoding () { return "&apos;"; }
		/** @return '\'' */
		public char getChar () { return '\''; }
	},
	quot {
		/** @return "&quot;" */
		public String getEncoding () { return "&quot;"; }
		/** @return '"' */
		public char getChar () { return '"'; }
	},
	amp {
		/** @return "&amp;" */
		public String getEncoding () { return "&amp;"; }
		/** @return '&' */
		public char getChar () { return '&'; }
	};
	
	/** get the XML encoding of the member */
	public abstract String getEncoding ();
	/** get the unencoded character */
	public abstract char getChar ();
	
	private static Logger olog_generic = Logger.getLogger ( "littleware.base.XmlSpecial" );

	/**
	 * Check whether the given character matches one
	 * of the XmlSpecial getChar values via a fast switch.
	 * Return the match or null if no match
	 *
	 * @param c_in character to check if it is special
	 * @return XmlSpecial such that getChar() == c_in
	 */
	public static XmlSpecial encode ( char c_in ) {
		switch ( c_in ) {
			case '<': {
				return lt;
			} 
			case '>': {
				return gt;
			} 
			case '\'': {
				return apos;
			} 
			case '"': {
				return quot;
			} 
			case '&': {
				return amp;
			} 
			default: {
				return null;
			} 
		}
	}
	
	/**
	 * Scan the given CharSequence, and return a String
	 * that replaces each occurence of an unescaped XmlSpecial.getChar()
	 * character with the corresponding XmlSpecial.getEncoding() encoding string.
	 *
	 * @param v_scan to scan
	 * @return string with special characters encoded
	 */
	public static String encode ( CharSequence v_scan ) {
		int        i_length = v_scan.length ();
		int        i_first_special = -1;

		for ( int i=0; (i < i_length) && (i_first_special == -1); ++i ) {
			XmlSpecial n_special = encode ( v_scan.charAt ( i ) );
			if ( null != n_special ) {
				i_first_special = i;
			}
		}
		if ( i_first_special > -1 ) {
			StringBuilder s_result = new StringBuilder ( i_length * 2 );
			s_result.append ( v_scan.subSequence ( 0, i_first_special ) );
			for ( int i=i_first_special; i < i_length; ++i ) {
				XmlSpecial n_special = encode ( v_scan.charAt ( i ) );
				if ( null == n_special ) {
					s_result.append ( v_scan.charAt( i ) );
				} else {
					s_result.append ( n_special.getEncoding () );
				}
			}
			return s_result.toString ();
		} else {
			return v_scan.toString ();
		}
	}
	
	/**
	 * Check whether the CharSequence beginning at v_char[i_start]
	 * matches one of the XmlSpecial.getEncoding() encodings,
	 * and return the XmlSpecial value if there is a match
	 *
	 * @param v_char character array
	 * @param i_start index into v_char to check for match against
	 */
	private static XmlSpecial decode ( CharSequence v_char, int i_start ) {
		if ( (v_char.charAt ( i_start ) != '&') 
			 || (v_char.length () < i_start + 4) 
			 )
		{
			return null;
		}
		
		switch ( v_char.charAt ( i_start + 1 ) ) {
			case 'l': {
				if ( (v_char.charAt ( i_start + 2) == 't')
					 && v_char.charAt ( i_start + 3 ) == ';' ) {
					return lt;
				}
			} break;
			case 'g': {
				if ( (v_char.charAt ( i_start + 2) == 't')
					 && v_char.charAt ( i_start + 3 ) == ';' ) {
					return gt;
				}
			} break;
			case 'q': {
				if ( 
					 v_char.length () > i_start + 5
					 && (v_char.charAt ( i_start + 2) == 'u')
					 && (v_char.charAt ( i_start + 3) == 'o')
					 && (v_char.charAt ( i_start + 4) == 't')
					 && v_char.charAt ( i_start + 5 ) == ';' ) {
					return quot;
				} 
			} break;
			case 'a': {
				switch ( v_char.charAt ( i_start + 2 ) ) {
					case 'm': {
						if ( v_char.length () > i_start + 4
							 && v_char.charAt ( i_start + 3 ) == 'p'
							 && v_char.charAt ( i_start + 4 ) == ';'
							 ) {
							return amp;
						}
					} break;
					case 'p': {
						if ( v_char.length () > i_start + 5
							 && v_char.charAt ( i_start + 3 ) == 'o'
							 && v_char.charAt ( i_start + 4 ) == 's'
							 && v_char.charAt ( i_start + 5 ) == ';'
							 ) {
							return apos;
						}
					} break;
				}
			}
		}
		return null;
	}
	
	/**
	 * Scan the given input string, and return a string
	 * with every occurence
	 * of an XmlSpecial encoding sequece "&bla;"
	 * with the corresponding XmlSpecial unencoded character.
	 */
	public static String decode ( CharSequence v_char ) {
		int i_length = v_char.length ();
		int i_last_semi = 0;
		
		// Find the last semicolon in the CharSequence
		for ( int i=i_length-1; (i > 0) && (0 == i_last_semi); --i ) {
			if ( v_char.charAt( i ) == ';' ) {
				i_last_semi = i;
			}
		}
		
		if ( i_last_semi > 2 ) {
			StringBuilder s_result = new StringBuilder ( i_length );
			for ( int i=0; i < i_last_semi; ++i ) {
				XmlSpecial n_special = decode ( v_char, i );
				if ( null == n_special ) {
					s_result.append ( v_char.charAt( i ) );
				} else {
					s_result.append ( n_special.getChar () );
					// skip over the rest of the encoding string
					i += n_special.getEncoding ().length () - 1;
				}
			}
			s_result.append ( v_char.subSequence ( i_last_semi + 1, i_length ) );
			return s_result.toString ();
		} else {
			return v_char.toString ();
		}
	}
    
    /**
     * Encode/decode the text from the specified source.
     * Checkout resources/XmlSpecialHelp_en.txt 
     *
     * @param v_argv
     */
    public static void main ( String[] v_argv ) {
        ResourceBundle bundle_support = ResourceBundle.getBundle ( "littleware.base.resources.XmlSpecialResources" );


        // create the command line options that we are looking for
		final LongOpt[] v_longopts = {
            new LongOpt(bundle_support.getString("version.option"), LongOpt.NO_ARGUMENT, null, 1),
			new LongOpt(bundle_support.getString("help.option"), LongOpt.NO_ARGUMENT, null, 'h'),
			new LongOpt(bundle_support.getString("encode.option"), LongOpt.NO_ARGUMENT, null, 'e'),
            new LongOpt(bundle_support.getString("decode.option"), LongOpt.NO_ARGUMENT, null, 'd')
		};

		final Getopt opts = new Getopt(bundle_support.getString("appname"), v_argv, "hed", v_longopts);
        
        boolean b_encode = true;
        
		for ( int i_opt = opts.getopt();
              i_opt != -1;
              i_opt = opts.getopt ()
              ){
			switch( i_opt ){
                case 1: {
                    System.out.println ( "littleware.base.XmlSecial version 0.1" );
                    System.exit( 0 );
                } break;
				case 'h':{
                    try {
                        final InputStream istream_help = XmlSpecial.class.getResourceAsStream( bundle_support.getString( "help.txt" ) );
                        final Reader      read_help = new InputStreamReader ( istream_help, Charset.forName( "UTF-8" ) );
                        final String      s_help = Whatever.readAll ( read_help );
                        
                        System.out.print ( s_help );
                        System.exit(0);                        
                    } catch ( IOException e ) {
                        throw new RuntimeException ( "Failure loading help data", e );
                    } 
				} break;
				case 'd':{
                    b_encode = false;
				} break;
                case 'e': break;
                default: {
                    System.err.println ( "Illegal option, try -h for help" );
                    System.exit( 1 );
                }
			}
		}
        String s_source = "stdin";
        if ( opts.getOptind() < v_argv.length ) {
            s_source = v_argv[ opts.getOptind () ].toLowerCase ();
        }
        if ( s_source.equals ( "stdin" ) ) {
            try {
                BufferedReader read_stdin = new BufferedReader ( new InputStreamReader ( System.in ) );
                for ( String s_data = read_stdin.readLine ();
                      s_data != null;
                      s_data = read_stdin.readLine ()
                      ) {
                    if ( b_encode ) {
                        System.out.println ( encode ( s_data ) );
                    } else {
                        System.out.println ( decode ( s_data ) );
                    }
                }
            } catch ( IOException e ) {
                throw new RuntimeException ( "Failure reading stdin", e );
            }
        } else { // clipboard
            Clipboard clip_master = Toolkit.getDefaultToolkit().getSystemClipboard();
            try {
                Transferable transfer_in = clip_master.getContents ( null );
                String       s_data = (String) transfer_in.getTransferData ( DataFlavor.stringFlavor );
                olog_generic.log ( Level.FINE, "Got from clipboard: " + s_data );
                if ( null != s_data ) {
                    final String s_out;
                    if ( b_encode ) {
                        s_out = encode ( s_data );
                    } else {
                        s_out = decode ( s_data );
                    }
                    Transferable transfer_string = new StringSelection ( s_out );
                    clip_master.setContents ( transfer_string, null );
                } else {
                    olog_generic.log ( Level.FINE, "null clipboard contents" );
                }
            } catch ( RuntimeException e ) {
                throw e;
            } catch ( Exception e ) {
                throw new RuntimeException ( "Failed clipboard read/write", e );
            }
        }
        System.exit( 0 );	
    }
}
			
// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

