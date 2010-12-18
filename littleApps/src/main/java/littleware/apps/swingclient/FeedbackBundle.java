/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.swingclient;

import com.google.inject.ProvidedBy;
import com.google.inject.Provider;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import littleware.base.feedback.Feedback;

/**
 * Container for building multiple feedback components in one simple call.
 * Caller is responsible for puting components into display where
 * appropriate.
 */
@ProvidedBy(SwingFeedbackBuilder.class)
public interface FeedbackBundle extends Provider<Feedback> {

    /**
     * Get progress bar feedback connects to
     */
    public JProgressBar getProgress();

    /**
     * Get textArea feedback connects to
     */
    public JTextArea getText();

    /**
     * Title feedback connects to
     */
    public JLabel getTitle();

    /**
     * Feedback connected to progress, title, and text
     */
    public Feedback getFeedback();
}
