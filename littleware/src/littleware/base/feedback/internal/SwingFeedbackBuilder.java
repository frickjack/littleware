/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.base.feedback.internal;

import littleware.base.feedback.FeedbackBundle;
import com.google.inject.Provider;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import littleware.base.feedback.LittleEvent;
import littleware.base.feedback.LittleListener;
import littleware.base.swing.SwingAdapter;
import littleware.base.feedback.UiMessageEvent;
import littleware.base.Whatever;
import littleware.base.feedback.Feedback;
import littleware.base.feedback.NullFeedback;
import littleware.base.swing.JTextAreaAppender;

/**
 * Simple Swing feedback component logs progress to a
 * progress bar and text area ensuring that UI updates
 * run on the Swing dispatch thread.
 * Builder just builds a null feedback with listeners
 * registered to update the given components.
 */
public class SwingFeedbackBuilder implements Provider<FeedbackBundle> {

    private static final Logger log = Logger.getLogger(SwingFeedbackBuilder.class.getName());

    /**
     * Little factory just allocations a LoggerUiFeedback
     * instance wired up with listeners to update
     * a given progress bar, label, and appender.
     *
     * @param jprogress maximum property set to 100 as side effect
     * @param jlabelTitle
     * @param appender
     * @return feedback instance
     */
    public Feedback build(final JProgressBar jprogress,
            final JLabel jlabelTitle,
            final Appendable appender,
            final Logger log) {
        jprogress.setMaximum(100 + 1);
        final NullFeedback feedback = new NullFeedback();
        SwingAdapter.get().dispatchWrap(
                new PropertyChangeListener() {

                    @Override
                    public void propertyChange(final PropertyChangeEvent evt) {
                        if (evt.getPropertyName().equals("title")) {
                            String sTitle = (String) evt.getNewValue();
                            if (sTitle.length() > 40) {
                                sTitle = sTitle.substring(0, 40);
                            }
                            jlabelTitle.setText(sTitle);
                        } else if (evt.getPropertyName().equals("progress")) {
                            jprogress.setValue((Integer) evt.getNewValue());
                        }
                    }
                }, feedback);

        feedback.addLittleListener(
                new LittleListener() {

                    @Override
                    public void receiveLittleEvent(final LittleEvent evtIn) {
                        if (!SwingUtilities.isEventDispatchThread()) {
                            SwingUtilities.invokeLater(
                                    new Runnable() {

                                        @Override
                                        public void run() {
                                            receiveLittleEvent(evtIn);
                                        }
                                    });
                            return;
                        }

                        if (evtIn instanceof UiMessageEvent) {
                            final UiMessageEvent mess = (UiMessageEvent) evtIn;
                            log.log(mess.getLevel(), mess.getMessage());
                            if (mess.getLevel().intValue() >= Level.INFO.intValue()) {
                                try {
                                    appender.append(mess.getMessage() + Whatever.NEWLINE);
                                } catch (IOException ex) {
                                    log.log(Level.WARNING, "Failed to append feedback", ex);
                                }
                            }
                        }
                    }
                });
        return feedback;
    }

    /** Alternate builder uses default logger */
    public Feedback build(final JProgressBar jprogress,
            final JLabel jlabelTitle,
            final Appendable appender) {
        return build(jprogress, jlabelTitle, appender, log);
    }

    @Override
    public FeedbackBundle get() {
        final JTextArea jtext = new JTextArea(3, 50);
        final JProgressBar jprogress = new JProgressBar();
        final JLabel jtitle = new JLabel();
        final Appendable appender = new JTextAreaAppender(jtext, 10000);
        final Feedback fb = build(jprogress, jtitle, appender);
        return new FeedbackBundle() {

            @Override
            public JProgressBar getProgress() {
                return jprogress;
            }

            @Override
            public JTextArea getText() {
                return jtext;
            }

            @Override
            public JLabel getTitle() {
                return jtitle;
            }

            @Override
            public Feedback getFeedback() {
                return fb;
            }

            @Override
            public Feedback get() {
                return getFeedback();
            }
        };
    }
}
