package littleware.apps.client;

import java.util.logging.Level;


/**
 * Implementation may be passed as parameter
 * or set as object property to allow potentially
 * long running.  Subtypes may provide methods
 * for delivering partial results back to the
 * UI as a computation progresses.
 */
public interface UiFeedback {
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
      * Title property for the task whose progress
      * the UI is tracking.
      */
     public String getTitle();
     public void setTitle( String s_title );
     
     /**
      * Log a message to the UI.
      * May be delivered asynchronously or bundled
      * or filtered whatever depending on the 
      * implementation.
      * 
      * @param level of message - may assist the UI
      *                in filtering messages of
      *                varying importance.
      * @param s_info message
      */
     public void log( Level level, String s_info );
     /** Shortcut for log( Level.INFO, s_info ) */
     public void info( String s_info );
}
