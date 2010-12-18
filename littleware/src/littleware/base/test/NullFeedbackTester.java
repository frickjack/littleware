/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.base.test;

import com.google.inject.Inject;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;
import junit.framework.TestCase;
import littleware.base.feedback.Feedback;
import littleware.base.feedback.NullFeedback;

/**
 *
 * @author pasquini
 */
public class NullFeedbackTester extends TestCase {
    private static final Logger log = Logger.getLogger( NullFeedbackTester.class.getName() );

    private final NullFeedback feedback;

    @Inject
    public NullFeedbackTester( NullFeedback feedback ) {
        super( "testFeedback" );
        this.feedback = feedback;
    }

    public static class Listener implements PropertyChangeListener {
        private int progress = -1;

        public int getProgress() { return progress; }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if ( evt.getPropertyName().equals( "progress" ) ) {
                progress = (Integer) evt.getNewValue();
            }
        }
    }

    public void testFeedback() {
        final Listener listener = new Listener();
        feedback.addPropertyChangeListener(listener);
        feedback.setProgress(10);
        assertTrue( "Progress set to 10: " + feedback.getProgress(),
                feedback.getProgress() == 10
                );
        assertTrue( "Listener received progress update to 10: " + listener.getProgress(),
                listener.getProgress() == feedback.getProgress()
                );
        final Feedback nested = feedback.nested(10, 100);
        nested.setProgress( 100, 200 );
        assertTrue( "Nested progress scales to 50: " + nested.getProgress(),
                nested.getProgress() == 50
                );
        assertTrue( "Nested progress propogates to parent: " + feedback.getProgress(),
                feedback.getProgress() == 15
                );
        assertTrue( "Parent listener receives nested progress update: " + listener.getProgress(),
                listener.getProgress() == feedback.getProgress()
                );
        feedback.setProgress( 301, 301 );
        assertTrue( "Feedback progress set to 100: " + feedback.getProgress(),
                feedback.getProgress() == 100
                );
        assertTrue( "Listener receives max notification: " + listener.getProgress(),
                listener.getProgress() == feedback.getProgress()
                );
    }
}
