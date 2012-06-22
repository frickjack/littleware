/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.message
package server
package internal

import com.google.gson
import com.google.inject
import java.beans
import java.util.EventObject
import littleware.base.{event => levent}
import littleware.base.feedback


/**
 * Listener for events from a littleware.base.feedback.Feedback instance
 * associated with the processing a given message.  Works with MessageProcessor.
 */
trait FeedbackListener extends levent.LittleListener with beans.PropertyChangeListener {}

object FeedbackListener {
  private class SimpleListener( 
    processor:SimpleMessageProcessor, 
    handle:model.MessageHandle,
    responseFactory:inject.Provider[model.Response.Builder],
    gsonFactory:inject.Provider[gson.Gson]
  ) extends FeedbackListener {
    private lazy val gsonTool = gsonFactory.get
  
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
          val payload:model.Payload = pubObj match {
            case pl:model.Payload => pl
            case _ => new model.JsonPayload( pubObj.getClass.getName, gsonTool.toJsonTree( pubObj ))
          }
          processor.postResponse( 
            handle,
            responseFactory.get.progress( fb.getProgress
            ).addResult( payload ).build                     
          )
        }
      case _ => {}
    }
  }
  
  class Builder @inject.Inject() (
    responseFactory:inject.Provider[model.Response.Builder],
    gsonFactory:inject.Provider[gson.Gson]    
  ) {
    var processor:SimpleMessageProcessor = null
    def processor( value:SimpleMessageProcessor ):this.type = {
      processor = value
      this
    }
    
    var handle:model.MessageHandle = null
    def handle( value:model.MessageHandle ):this.type = {
      handle = value
      this
    }
    
    def build():FeedbackListener = {
      require( null != processor, "processor not null" )
      require( null != handle, "hanlde not null" )
      new SimpleListener( processor, handle, responseFactory, gsonFactory )
    }
  }
}