/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.base.test;

import com.google.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import littleware.base.feedback.internal.SwingFeedbackBuilder;
import littleware.base.feedback.Feedback;
import littleware.base.swing.JTextAppender;
import littleware.test.JLittleDialog;
import littleware.test.LittleTest;

/**
 * Test SwingUiFeedback
 */
public class SwingFeedbackTester extends LittleTest {
    private static final Logger olog = Logger.getLogger( SwingFeedbackTester.class.getName() );
    private final SwingFeedbackBuilder obuilder;

    public SwingFeedbackTester () {
        obuilder = new SwingFeedbackBuilder();
    }
    
    @Inject
    public SwingFeedbackTester( SwingFeedbackBuilder builder ) {
        obuilder = builder;
        setName( "testSwingFeedback" );
    }

    public void testSwingFeedback() {
        final JPanel jpanelTest = new JPanel();
        final BoxLayout layout = new BoxLayout( jpanelTest, BoxLayout.Y_AXIS);
        jpanelTest.setLayout(layout);
        final JLabel       jlabel = new JLabel( "FAIL TEST IF YOU SEE THIS!" );
        final JProgressBar jprogress = new JProgressBar();
        final JTextAppender jappend = new JTextAppender();
        jpanelTest.add( jprogress );
        jpanelTest.add(jappend);
        final Feedback feedback = obuilder.build(jprogress, jlabel, jappend);
        new Thread( new Runnable() {

            @Override
            public void run() {
                feedback.setTitle( "Title Test OK" );
                for( int i=1; i < 11; ++i ) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        olog.log(Level.SEVERE, "Feedback thread interrupted", ex);
                    }
                    feedback.info( "Loop " + i + " of 10" );
                    feedback.setProgress( i * 10 );
                }
            }
        } ).start ();
        assertTrue("User confirmed SwingFeedback UI functional",
                    JLittleDialog.showTestDialog( jpanelTest,
                    "verify feedback update")
                    );
    }

    public static void main ( String[] vArgv ) {
        String[] vLaunchArgs = { SwingFeedbackTester.class.getName() };

        junit.swingui.TestRunner.main(vLaunchArgs);

    }
}
