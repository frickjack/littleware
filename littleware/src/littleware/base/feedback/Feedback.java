/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.base.feedback;

import com.google.inject.ImplementedBy;
import java.util.logging.Level;


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
      * @param x_result partial result 
      */
     public void publish( Object x_result );

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
      * @param s_info message
      */
     public void log( Level level, String s_info );
     /** Shortcut for log( Level.INFO, s_info ) */
     public void info( String s_info );
}
