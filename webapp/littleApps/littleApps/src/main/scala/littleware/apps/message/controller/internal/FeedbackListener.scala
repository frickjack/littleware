/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.message
package controller
package internal

import com.google.inject
import java.beans
import java.util.EventObject
import littleware.base.{event => levent}
import littleware.base.feedback


/**
 * Listener for events from a littleware.base.feedback.Feedback instance
 * associated with the processing a given message.  Works with MessageProcessor.
 */
class FeedbackListener( 
  processor:SimpleMessageProcessor, 
  handle:model.MessageHandle,
  responseFactory:inject.Provider[model.Response.Builder]
) extends levent.LittleListener with beans.PropertyChangeListener {
  override def receiveLittleEvent( event:levent.LittleEvent ):Unit = processEvent( event )
  override def propertyChange( event:beans.PropertyChangeEvent ):Unit = processEvent( event )
    
  def processEvent( event:EventObject ):Unit = event match {
    case progEv:beans.PropertyChangeEvent if ( progEv.getPropertyName == "progress" ) => {          
        val progress = progEv.getNewValue.asInstanceOf[java.lang.Integer]
        processor.postResponse(handle, 
                               responseFactory.get.progress( progEv.getNewValue.asInstanceOf[java.lang.Integer]
          ).build 
        )
      }
    case fbEv:feedback.UiMessageEvent if ( fbEv.getLevel.intValue >= java.util.logging.Level.INFO.intValue ) => {
        val mess = fbEv.getMessage
        val fb = fbEv.getSource.asInstanceOf[feedback.Feedback]
        processor.postResponse( handle,
                               responseFactory.get.progress( fb.getProgress 
          ).addFeedback(mess).build
        )
      }
    case pubEv:feedback.UiPublishEvent => {
        val pubObj = pubEv.getData
        val fb = pubEv.getSource.asInstanceOf[feedback.Feedback]
        processor.postResponse( 
          handle,
          responseFactory.get.progress( fb.getProgress
          ).addResult( pubObj ).build                     
        )
      }
    case _ => {}
  }
}
