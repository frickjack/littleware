/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.test;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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

    private static final Logger log = Logger.getLogger(JLittleDialog.class.getName());
    private static final long serialVersionUID = -3476553626952709775L;

    /**
     * Possible test results:  <br />
     *    NORESULT = no result is ready - dialog may still be up, <br />
     *    PASSED = "Test Passed" button clicked <br />
     *    FAILED = "Test Failed" button clicked <br />
     */
    public enum Result {

        NORESULT, PASSED, FAILED;
    }
    private Result on_result = Result.NORESULT;

    /**
     * Specialization of JDialog displays a test component.
     * Constructor packs dialog content pane with test component,
     * and buttons for 'Test Passed' and 'Test Failed' that
     * setLastTestResult and dispose() of the Dialog.
     *
     * @param wcomp_test to popup in the test dialog
     * @param s_instructions to place above wcomp_test in the dialog
     */
    public JLittleDialog(Component wcomp_test, String s_instructions) {
        getContentPane().setLayout(new BorderLayout());
        //getContentPane ().setLayout ( new BoxLayout( getContentPane(), BoxLayout.Y_AXIS ) );

        JLabel wlabel_instruct = new JLabel("<html><body><p>" + s_instructions + "</p></body></html>");
        JPanel wpanel_buttons = new JPanel();
        JButton wbutton_failed = new JButton("Test Failed");
        JButton wbutton_passed = new JButton("Test Passed");

        wbutton_failed.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event_button) {
                setLastResult(Result.FAILED);
                JLittleDialog.this.dispose();
            }
        });
        wbutton_passed.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event_button) {
                setLastResult(Result.PASSED);
                JLittleDialog.this.dispose();
            }
        });

        wpanel_buttons.add(wbutton_failed);
        wpanel_buttons.add(wbutton_passed);

        getContentPane().add(wlabel_instruct, BorderLayout.NORTH);
        getContentPane().add(wcomp_test, BorderLayout.CENTER);
        getContentPane().add(wpanel_buttons, BorderLayout.SOUTH);

        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent evt_win) {
                setLastResult(Result.FAILED);
            }
        });
    }

    /**
     * Get the last value assigned to the result
     */
    public Result getLastResult() {
        return on_result;
    }

    /**
     * Internal utility - setLastResult, and notify those waiting for it.
     */
    private synchronized void setLastResult(Result n_result) {
        on_result = n_result;
        if (!n_result.equals(Result.NORESULT)) {
            notifyAll();
        }
    }

    /**
     * Return the result of getLastResult() after
     * waiting for the result to change from NORESULT
     * if necessary.
     *
     * @return getLastResult when ready
     */
    public synchronized Result waitForResult() {
        Result n_result = getLastResult();
        while (n_result.equals(Result.NORESULT)) {
            try {
                wait();
            } catch (InterruptedException e) {
                log.log(Level.WARNING, "Ignoring interrupt: " + e);
            }
            n_result = getLastResult();
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
        if (!isVisible()) {
            setLastResult(Result.NORESULT);
            Runnable run_dialog = new Runnable() {

                @Override
                public void run() {
                    setModal(false);
                    pack();
                    setVisible(true);
                }
            };

            if ( SwingUtilities.isEventDispatchThread() ) {
                log.log(Level.FINE, "Launching dialog on Swing thread ...");
                setModal(true);
                pack();
                setVisible(true);
            } else {
                SwingUtilities.invokeLater(run_dialog);
            }
        }

        return waitForResult();
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
    public static boolean showTestDialog(final JComponent jtest, final String instructions) {
        final JLittleDialog wdial_test = new JLittleDialog(jtest, instructions);
        return wdial_test.showDialogAndWait().equals(JLittleDialog.Result.PASSED);
    }
}
