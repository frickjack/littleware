package littleware.base.swing;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.Document;
import java.util.SortedSet;
import java.io.PrintWriter;

import littleware.base.*;


/**
 * Swing component wrapper around a ScriptRunner implementation -
 * provides a view of a ScriptRunner.
 */
public class JScriptRunner extends JPanel implements ScriptRunner {
	public  static final int   OI_DEFAULT_HEIGHT = 10;
	public  static final int   OI_DEFAULT_WIDTH = 40;
	
	private   ScriptRunner  om_script = null;
	private   JTextArea     ow_text = null;
	private   JComboBox     ow_lang = null;
    private   int           oi_buffer_size = 0;
	
	/**
	 * Shared constructor code
	 *
	 * @param i_height rows in text area hint
	 * @param i_width columns in text area hint
	 */
	private void init ( int i_height, int i_width ) {
		if ( i_width <= 0 ) {
			i_width = OI_DEFAULT_WIDTH;
		} 
		if ( i_height <= 0 ) {
			i_height = OI_DEFAULT_HEIGHT;
		}
		
		this.setLayout ( new GridBagLayout () );
		JPanel    w_controls = new JPanel ();
		{
			/**
			 * Setup a combox box to change widget mode
			 */
			w_controls.setBorder( BorderFactory.createEmptyBorder(30, 30, 10, 30) );
			w_controls.setLayout( new FlowLayout ( FlowLayout.RIGHT ) );
			
			ow_lang = new JComboBox( om_script.getSupportedLanguages ().toArray () );
			ow_lang.setSelectedIndex( 0 );
			ow_lang.addActionListener(
									 new ActionListener () {
										 /** Listens to the combo box. */
										 public void actionPerformed(ActionEvent event_x ) {
											 String s_lang = (String) ow_lang.getSelectedItem();
											 try {
												 setLanguage ( s_lang );
											 } catch ( ScriptException e ) {
												 JOptionPane.showMessageDialog ( JScriptRunner.this, "Invalid language, " + s_lang + ", caught: " + e,
																				 "setLanguage error", JOptionPane.ERROR_MESSAGE
																				 );
											 }
										 }
									 }										 
									 );
			w_controls.add ( ow_lang );
			
			/** Pull label from resource-bundle later */
			JButton w_clear = new JButton( "Clear" );
			w_clear.addActionListener ( 
										 new ActionListener () {
				public void actionPerformed(ActionEvent e) {
					JScriptRunner.this.clear ();
				}
										 }
				);
			w_controls.add ( w_clear );
			
			JButton w_exec = new JButton ( "Exec" );
			w_exec.addActionListener ( 
				new ActionListener () {
					public void actionPerformed( ActionEvent event_x ) {
						Document doc_text = ow_text.getDocument ();
						try {
							String   s_script = doc_text.getText ( 0, doc_text.getLength () );
							JScriptRunner.this.exec ( s_script );
						} catch ( Exception e ) {
							JOptionPane.showMessageDialog ( JScriptRunner.this, "Caught: " + e,
															"Script Error", JOptionPane.ERROR_MESSAGE
															);
						}
					}
				}
									   );
			w_controls.add ( w_exec );
		}		
		
		ow_text = new JTextArea( i_height, i_width );
        ow_text.setEditable( true );
		
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
	
	/** 
	 * Constructor sets up default text area and scrollable panel.
	 * Uses default ScriptRunner from ScriptRunnFactory.getFactory ().create ();
	 */
	public JScriptRunner () {
		om_script = ScriptRunnerFactory.getFactory ().create ();
		init ( OI_DEFAULT_HEIGHT, OI_DEFAULT_WIDTH );
	}
	
	/** 
	 * Constructor with user-supplied height/width hint.
	 * Buffer-size gets set to i_height + i_width + OI_DEFAULT_BUFFER.
	 *
	 * @param m_script ScriptRunner to call through to
	 * @param i_height in columns
     * @param i_width in rows
	 */
	public JScriptRunner ( ScriptRunner m_script, int i_height, int i_width ) {
		om_script = m_script;
		init ( i_height, i_width );
	}
	
		
	
	public String getLanguage () {
		return om_script.getLanguage ();
	}
	
	public void setLanguage ( String s_lang ) throws ScriptException {
		om_script.setLanguage ( s_lang );
		
 		/** 
		 * Need to wire in some thread awareness
		 */
		SwingUtilities.invokeLater ( 
									   new Runnable () {
										   public void run () {
 											   ow_lang.setSelectedItem ( om_script.getLanguage () );
										   }
									   }
									   );
	}
	
	public SortedSet<String> getSupportedLanguages () {
		return om_script.getSupportedLanguages ();
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
		}
	}
	
	
	/**
	 * Add the given string to the end of the code-entry area.
	 */
    public void append ( CharSequence s_in ) {
		Runnable run_handler = 	new AppendAction ( s_in.toString () );
			
		SwingUtilities.invokeLater ( run_handler );
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
	
	public PrintWriter getErrorWriter () {
		return om_script.getErrorWriter ();
	}
	
	public void setErrorWriter ( PrintWriter write_error )
	{
		om_script.setErrorWriter ( write_error );
	}
	
	public PrintWriter getWriter () {
		return om_script.getWriter ();
	}

	public void setWriter ( PrintWriter write_out )
	{
		om_script.setWriter ( write_out );
	}
		

	public void exec ( String s_script ) throws ScriptException {
		clear ();
		append ( s_script );
		om_script.exec ( s_script );
	}
	
	
	public Object registerBean ( String s_name, Object x_bean )
	{
		return om_script.registerBean ( s_name, x_bean );
	}
	
	public Object clearBean ( String s_name ) {
		return om_script.clearBean ( s_name );
	}
	
	public Object getBean ( String s_bean ) {
		return om_script.getBean ( s_bean );
	}
	
	/**
	 * Separate function to run on AWT event-dispatch thread
	 */
	private static void createAndShowGUI () {
		JTextAppender w_appender = new JTextAppender ();
		JScriptRunner w_runner = new JScriptRunner ();
		JPanel        w_fullapp = JUtil.addAppenderToRunner ( w_appender, w_runner );	
		JFrame        w_root = new JFrame ( "JScriptRunner" );
        
        w_root.getContentPane ().add ( w_fullapp );
        w_root.setDefaultCloseOperation ( JFrame.EXIT_ON_CLOSE );
        w_root.pack ();
        w_root.setVisible ( true );
	}
	
	/**
	 * Launch a little application that lets the user
	 * run scripts for testing.
	 */
	public static void main ( String[] v_argv ) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
											public void run() {
												createAndShowGUI();
											}
		}
											   );
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

