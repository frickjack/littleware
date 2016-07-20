package littleware.base.feedback;

import com.google.inject.ImplementedBy;
import java.util.logging.Level;
import littleware.base.event.LittleTool;


/**
 * Simple UI-feedback mechanism for long-running tasks.
 */
@ImplementedBy(LoggerFeedback.class)
public interface Feedback {
    /** 
     * Property tracks the progress of some
     * task on a scale from 0 to 100
     */
    public int getProgress();
    /** 
     * Property is force to be between (0,100)
     */
     public void setProgress( int i_progress );
     /**
      * Set property normalizing i_progress to a 
      * scale (0,i_max)
      */
     public void setProgress( int i_progress, int i_max );

     /**
      * Build a nested Feedback instance configured to
      * advance over the next progress/max units of the
      * parent progress bar.
      */
     public Feedback nested( int progress, int max );
     
     /**
      * Title property for the task whose progress
      * the UI is tracking.
      */
     public String getTitle();
     public void setTitle( String s_title );
     
     /**
      * Allow the worker to publish a partial result back to
      * the UI as computation continues.  Fires UiPublishEvent -
      * listener should check result-type to verify it's what
      * the listener expects as a feedback object might get
      * passed around between different worker methods.
      * 
      * @param result partial result 
      */
     public void publish( Object result );

     /**
      * Log a message to the UI.
      * May be delivered asynchronously or bundled
      * or filtered whatever depending on the 
      * implementation.  Fires littleware.client.event.UiMessageEvent
      * to LittleListeners.
      * 
      * @param level of message - may assist the UI
      *                in filtering messages of
      *                varying importance.
      * @param info message
      */
     public void log( Level level, String info );
     /** Shortcut for log( Level.INFO, s_info ) */
     public void info( String info );
     
     /**
      * Builder provides mechanisms to register listeners
      * with built feedback instance
      */
     @ImplementedBy(LoggerFeedback.Builder.class)
     public static interface Builder extends LittleTool {
         public Feedback build();
     }
}
