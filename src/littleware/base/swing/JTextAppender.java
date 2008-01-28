package littleware.base.swing;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.Document;
import java.io.*;

import littleware.base.*;


/**
 * Sets up a view of an Appendable interface.
 * Appends to a text widget with a scroll bar and some simple controls.
 */
public class JTextAppender extends JPanel implements Appendable {	
	public  static final int   OI_DEFAULT_HEIGHT = 10;
	public  static final int   OI_DEFAULT_WIDTH = 40;
	/** Default buffer size (in characters) */
	public  static final int   OI_DEFAULT_BUFFER = 10240;
	
	
	/**
	 * Append mode
	 */
	public enum Mode {
		/** TAIL append mode - advance carrot when new data appended - this is default */
		TAIL,
		/** 
		 * PAUSE append mode - appended data lost when append PAUSED
		 */
		PAUSE,
		/**
	     * Continue to add appended data to the text area, but
		 * do not advance the caret
		 */
		GENERIC
	}
	
	private   JTextArea     ow_text = null;
	private   JComboBox     ow_mode = null;
    private   int           oi_buffer_size = 0;
	private   Mode          on_mode = Mode.TAIL;
	
	/**
	 * Shared constructor code
	 *
	 * @param i_height rows in text area hint
	 * @param i_width columns in text area hint
	 * @param i_buffer size - forced to be at least i_width * i_height
	 */
	private void init ( int i_height, int i_width, int i_buffer ) {
		if ( i_width <= 0 ) {
			i_width = OI_DEFAULT_WIDTH;
		} 
		if ( i_height <= 0 ) {
			i_height = OI_DEFAULT_HEIGHT;
		}
		if ( i_buffer < (i_width * i_height) ) {
			i_buffer = i_width * i_height;
		}

		oi_buffer_size = i_buffer;
		this.setLayout ( new GridBagLayout () );
		JPanel    w_controls = new JPanel ();
		{
			/**
			 * Setup a combox box to change widget mode
			 */
			w_controls.setBorder( BorderFactory.createEmptyBorder(30, 30, 10, 30) );
			w_controls.setLayout( new FlowLayout ( FlowLayout.RIGHT ) );
			
			ow_mode = new JComboBox( Mode.values () );
			ow_mode.setSelectedIndex( on_mode.ordinal () );
			ow_mode.addActionListener(
									 new ActionListener () {
										 /** Listens to the combo box. */
										 public void actionPerformed(ActionEvent e) {
											 Mode n_mode = (Mode) ow_mode.getSelectedItem();
											 setMode( n_mode );
										 }
									 }										 
									 );
			w_controls.add ( ow_mode );
			
			/** Pull label from resource-bundle later */
			JButton w_button = new JButton( "Clear" );
			w_button.addActionListener ( 
										 new ActionListener () {
				public void actionPerformed(ActionEvent e) {
					JTextAppender.this.clear ();
				}
										 }
				);
			w_controls.add ( w_button );
		}		
		
		ow_text = new JTextArea( i_height, i_width );
        ow_text.setEditable( false );
		
        JScrollPane w_scroll = new JScrollPane( ow_text,
												 JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
												 JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
												);
		
        //Add Components to this panel.
        GridBagConstraints w_gridbag = new GridBagConstraints();
        w_gridbag.gridwidth = GridBagConstraints.REMAINDER;
		w_gridbag.fill = GridBagConstraints.HORIZONTAL;
		this.add ( w_controls, w_gridbag );
		
        w_gridbag.fill = GridBagConstraints.BOTH;
		w_gridbag.weightx = 1.0;
		w_gridbag.weighty = 1.0;
        this.add( w_scroll, w_gridbag );		
	}
	
	/** Constructor sets up default text area and scrollable panel */
	public JTextAppender () {
		init ( OI_DEFAULT_HEIGHT, OI_DEFAULT_WIDTH, OI_DEFAULT_BUFFER );
	}
	
	/** 
	 * Constructor with user-supplied height/width hint.
	 * Buffer-size gets set to i_height + i_width + OI_DEFAULT_BUFFER.
	 */
	public JTextAppender ( int i_height, int i_width ) {
		init ( i_height, i_width, OI_DEFAULT_BUFFER );
	}
	
	/**
	 * Constructor with user supplied height, width, and buffer-size hint
	 *
	 * @param i_height hint in rows
	 * @param i_width hint in columns
	 * @param i_buffer hint in number of characters
	 */
	public JTextAppender ( int i_height, int i_width, int i_buffer ) {
		init ( i_height, i_width, i_buffer );
	}
	
	/**
	 * Get the JTextAppender mode
	 */
	public Mode getMode () { return on_mode; }
	
	/**
	 * Set the JTextAppender mode - update the mode display 
	 */
	public void setMode ( Mode n_mode ) {
		on_mode = n_mode;
		/** 
		 * Need to wire in some thread awareness
		 */
		SwingUtilities.invokeLater ( 
									   new Runnable () {
										   public void run () {
 											   ow_mode.setSelectedIndex( on_mode.ordinal () );
											   int i_length = ow_text.getDocument ().getLength ();
											   
											   if ( on_mode.equals ( Mode.GENERIC ) 
													&& (ow_text.getCaretPosition () == i_length)
													&& (i_length > 0)
													) {
												   ow_text.setCaretPosition( i_length - 1 );
											   }

										   }
									   }
									   );
	}
	
	public Appendable append ( char c ) {
		return this.append ( "" + c );
	}
	
	/**
	 * Little Runnable for append to pass to SwingUtilities.invokeLater
	 */
	private class AppendAction implements Runnable {
		private String  os_new = null;
				
		public AppendAction ( String s_in ) {
			os_new = s_in;
		}
		
		public void run () {
			ow_text.append ( os_new );
			
			int i_size = ow_text.getDocument().getLength ();
			if ( i_size > oi_buffer_size ) {
				ow_text.replaceRange ( "", 0, (int) i_size / 10 );
			}
			if ( on_mode.equals ( Mode.TAIL ) ) {
				ow_text.setCaretPosition( ow_text.getDocument().getLength() );
			}
		}
	}
	
	/**
	 * Streams the CharSequence off to the Swing dispatch thread
	 * asynchronously via SwingUtilities.invokeLater
	 */
    public Appendable append ( CharSequence s_in ) {
		if ( ! on_mode.equals ( Mode.PAUSE ) ) {
			Runnable run_handler = 	new AppendAction ( s_in.toString () );
			
			SwingUtilities.invokeLater ( run_handler );
		}
		
		return this;
	}
	
	public Appendable append ( CharSequence s_in, int i_start, int i_end ) {
		return this.append ( s_in.subSequence ( i_start, i_end ) );
	}

	/**
	 * Clear the contents of the display
	 */
	public void clear () {
		SwingUtilities.invokeLater (
				new Runnable () {
					public void run () {
						ow_text.replaceRange ( "", 0, ow_text.getDocument ().getLength () );
					}
				}
			);
	}
	
	/**
	 * Provide a hook to the Document model getting rendered.
	 * Do this rather than provide hook to underlying JTextArea -
	 * gives us flexibility to move to different underlying text widget later.
	 */
	public Document getDocument () {
		return ow_text.getDocument ();
	}
	
	
	/**
	 * Push into separate method to run on event-dispatch thread
	 */
	private static void createAndShowGUI () {
		JTextAppender w_appender = new JTextAppender ( OI_DEFAULT_HEIGHT, OI_DEFAULT_WIDTH, 102400 );
		final JFrame  w_root = new JFrame ( "JTextAppender" );
        Writer        write_appender = new BufferedWriter ( new AppenderWriter ( w_appender ) );
		Runnable      run_readerwriter = new ReaderWriter ( new BufferedReader ( new InputStreamReader ( System.in ) ),
															write_appender, 
															new Runnable () {
																public void run () {
																	JOptionPane.showMessageDialog ( w_root,
																									"End of input reached",
																									"end of stream",
																									JOptionPane.INFORMATION_MESSAGE
																									);
																}
															}
															);
		
		Thread thread_reader = new Thread ( run_readerwriter );
		thread_reader.start ();
		
        w_root.getContentPane ().add ( w_appender );
        w_root.setDefaultCloseOperation ( JFrame.EXIT_ON_CLOSE );
        w_root.pack ();
        w_root.setVisible ( true );
	}
		
	/**
	 * Launch a little application that sends System.in to 
	 * a JTextAppender
	 */
	public static void main ( String[] v_argv ) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
	}
	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

