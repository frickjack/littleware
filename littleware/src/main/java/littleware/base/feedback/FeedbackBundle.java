package littleware.base.feedback;

import com.google.inject.ProvidedBy;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;

/**
 * Container for building multiple feedback components in one simple call.
 * Caller is responsible for puting components into display where
 * appropriate.
 */
@ProvidedBy(littleware.base.feedback.internal.SwingFeedbackBuilder.class)
public interface FeedbackBundle extends com.google.inject.Provider<Feedback> {

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
