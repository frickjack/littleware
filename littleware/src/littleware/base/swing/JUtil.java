package littleware.base.swing;

import java.awt.*;
import javax.swing.*;
import java.io.*;

import littleware.base.*;


/**
 * Just a place to stuff some utility functions
 */
public abstract class JUtil {
	
	/**
	 * Little utility returns a new JPanel with 
	 * a JScriptRunner alligned over a JTextAppender
	 * with setWriter/setErrorWriter assigned to the ScriptRunner.
	 *
	 * @param w_appender to receive output
	 * @param w_script IN/OUT parameter - setWriter/setErrorWriter assigned
	 */
	public static JPanel addAppenderToRunner ( JTextAppender w_appender,
										JScriptRunner w_script
										)
	{
		PrintWriter   write_widget = new PrintWriter ( new AppenderWriter ( w_appender ) );
		
		w_script.setWriter ( write_widget );
		w_script.setErrorWriter ( write_widget );
		w_script.append ( "Enter script here (erase this line)\n" );
		w_appender.append ( "write to this window via bsf.stderr and bsf.stdout\n" );
		
		JPanel w_fullapp = new JPanel ();
		w_fullapp.setLayout( new GridBagLayout() );
		
		GridBagConstraints w_gridbag = new GridBagConstraints();
		w_gridbag.gridwidth = GridBagConstraints.REMAINDER;
		w_gridbag.fill = GridBagConstraints.HORIZONTAL;
		w_gridbag.weightx = 1.0;
		w_gridbag.weighty = 1.0;
		
		w_fullapp.add( w_script, w_gridbag );
		w_fullapp.add( w_appender, w_gridbag );
		return w_fullapp;
	}
    
    /**
     * Climb the getParent() tree until the root is found
     *
     * @param w_validate widget to retrieve root from
     * @return root component
     */
    public static Component findRoot ( java.awt.Component w_validate ) {
        Component w_root = w_validate;
        for ( Component w_search = w_validate.getParent ();
              (w_search != null) && (w_search != w_root);
              w_search = w_search.getParent ()
              ) {
            w_root = w_search;
        }
        return w_root;
    }
	

}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

