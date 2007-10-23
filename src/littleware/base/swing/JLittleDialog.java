package littleware.base.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.swing.*;


/**
 * Just a utility container to fascilitate launching
 * some different kinds of option dialogs.
 * Kind of a weird design - since we want callers to be able
 *      to wait for a result, but don't want to be
 *      MODAL - because want to allow test-widgets to
 *      popup interactive child windows.
 */
public class JLittleDialog extends JDialog {  
    private static final Logger  olog_generic = Logger.getLogger ( "littleware.base.swing.JLittleDialog" );
    /** 
     * Possible test results:  <br />
     *    NORESULT = no result is ready - dialog may still be up, <br />
     *    PASSED = "Test Passed" button clicked <br />
     *    FAILED = "Test Failed" button clicked <br />
     */
    public enum Result {
        NORESULT, PASSED, FAILED;
    }
    
    private Result    on_result = Result.NORESULT;
    
    /**
     * Specialization of JDialog displays a test component.
     * Constructor packs dialog content pane with test component,
     * and buttons for 'Test Passed' and 'Test Failed' that
     * setLastTestResult and dispose() of the Dialog.
     *
     * @param wcomp_test to popup in the test dialog
     * @param s_instructions to place above wcomp_test in the dialog
     */
    public JLittleDialog ( Component wcomp_test, String s_instructions ) 
    {
        getContentPane ().setLayout ( new BorderLayout () );
        
        JLabel      wlabel_instruct = new JLabel ( "<html><body><p>" + s_instructions + "</p></body></html>" );
        JPanel      wpanel_buttons = new JPanel ();
        JButton     wbutton_failed = new JButton ( "Test Failed" );
        JButton     wbutton_passed = new JButton ( "Test Passed" );
        
        wbutton_failed.addActionListener ( new ActionListener () {
            public void actionPerformed ( ActionEvent event_button ) {
                setLastResult( Result.FAILED );
                JLittleDialog.this.dispose ();
            }
        }
                                           );
        wbutton_passed.addActionListener ( new ActionListener () {
            public void actionPerformed ( ActionEvent event_button ) {
                setLastResult ( Result.PASSED );
                JLittleDialog.this.dispose ();
            }
        }
                                           );
        
        wpanel_buttons.add ( wbutton_failed );
        wpanel_buttons.add ( wbutton_passed );
        
        getContentPane ().add ( wlabel_instruct, BorderLayout.NORTH );
        getContentPane ().add ( wcomp_test, BorderLayout.CENTER );
        getContentPane ().add ( wpanel_buttons, BorderLayout.SOUTH );
    }
    
    /**
     * Get the last value assigned to the result
     */
    public Result getLastResult () {
        return on_result;
    }
    
    /**
     * Internal utility - setLastResult, and notify those waiting for it.
     */
    private synchronized void setLastResult ( Result n_result ) {
        on_result = n_result;
        if ( ! n_result.equals ( Result.NORESULT ) ) {
            notifyAll ();
        }
    }
    
    /**
     * Return the result of getLastResult() after
     * waiting for the result to change from NORESULT
     * if necessary.
     *
     * @return getLastResult when ready
     */
    public synchronized Result waitForResult () {
        Result n_result = getLastResult ();
        while ( n_result.equals ( Result.NORESULT ) ) {
            try {
                wait ();
            } catch ( InterruptedException e ) {
                olog_generic.log ( Level.WARNING, "Ignoring interrupt: " + e );
            }
            n_result = getLastResult ();
        }
        return n_result;
    }
    
    /**
     * Pack the dialog, and pop it up, return the post-test
     * getLastResult() value.
     *
     * @return getLastResult ()
     */
    public synchronized Result showDialogAndWait() {
        if ( ! isVisible () ) {
            setLastResult ( Result.NORESULT );
            Runnable  run_dialog = new Runnable () {
                public void run () {
                    setModal(false);
                    pack();
                    setVisible( true );                
                }
            };
            
            if ( SwingUtilities.isEventDispatchThread () ) {
                run_dialog.run ();
            } else {        
                try {
                    SwingUtilities.invokeLater ( run_dialog );
                } catch ( RuntimeException e ) {
                    throw e;
                } catch ( Exception e ) {
                    throw new RuntimeException ( "Failed to build UI", e );
                }
            }
        }
        
        return waitForResult ();
    }
    

	/**
	 * Launch a test-case dialog with instructions and
	 * an internal widget for the client to interactively test.
	 * The user indicates a successful test by hitting the
	 * dialog 'OK' button.
	 *
	 * @param w_test widget to test
	 * @param s_instructions to present to the user - gets wrapped in an html paragraph
	 * @return getLastResult ().equals ( Result.PASSED )
	 */
	public static boolean showTestDialog ( JComponent w_test, String s_instructions ) {
        JLittleDialog wdial_test = new JLittleDialog ( w_test, s_instructions );
        return wdial_test.showDialogAndWait ().equals ( JLittleDialog.Result.PASSED );
	}		
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

