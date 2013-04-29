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

import com.google.common.{cache => gcache}
import com.google.common.{collect => gcollect}
import com.google.common.{eventbus => gbus}
import com.google.common.util.concurrent.ListeningExecutorService
import com.google.inject
import java.beans
import java.util.UUID
import java.util.logging.{Level,Logger}
import littleware.base.{event => levent}
import littleware.base.feedback
import org.joda.{time => jtime}
import scala.collection.JavaConversions._

/**
 * Simple in-memory message processor leverages
 * injected executor service and feedback factory.
 * TODO - implement ApacheMQ or RabbitMQ or whatever processor.
 */
class SimpleMessageProcessor @inject.Inject()(
  exec:ListeningExecutorService,
  fbFactory:inject.Provider[feedback.Feedback.Builder],
  responseFactory:inject.Provider[model.Response.Builder],
  fbListenerFactory:inject.Provider[FeedbackListener.Builder],
  loginStrategy:LoginStrategy
) extends MessageProcessor {
  private val log = Logger.getLogger( getClass.getName )
  
  private lazy val _client:remote.MessageRemote = new SimpleMessageProcessor.SMPClient( this, loginStrategy )
  override def client:remote.MessageRemote = _client
  
  
  def getListeners( typeSpec:String ):Option[Class[_ <: MessageListener]] = try {
    Some( Class.forName( typeSpec ).asInstanceOf[Class[_ <: MessageListener]] )
  } catch {
     case ex:ClassNotFoundException => {
         log.warning( "Unable to find class loader for: " + typeSpec )
         None
     } 
  }
  
  
  
  /**
   * Cache maps client login session to set of message-handle ids associated
   * with that handle
   */
  val messageBySession:gcache.LoadingCache[UUID,java.util.Map[UUID,model.MessageEvent]] =
    gcache.CacheBuilder.newBuilder(
    ).expireAfterAccess( 5, java.util.concurrent.TimeUnit.MINUTES 
    ).build(
      new gcache.CacheLoader[UUID,java.util.Map[UUID,model.MessageEvent]] {
        override def load( sessionKey:UUID ):java.util.Map[UUID,model.MessageEvent] = 
          new gcollect.MapMaker().concurrencyLevel(4).makeMap()
      }
    )
  

  def postClientMessage( event:model.MessageEvent ):Unit = {
    val sessionId = event.session.sessionId
    val injector = loginStrategy.lookup( sessionId )
    val listeners:Option[MessageListener] = getListeners( 
        event.message.messageType 
      ).map( 
        (clazz) => injector.getInstance( clazz ) 
      )
    require( ! listeners.isEmpty, 
            "Listeners registers for posted message type: " + event.message.messageType
    )
    
    val minAge = jtime.DateTime.now.minusMinutes(6)
    val sessionMap:java.util.Map[UUID,model.MessageEvent] = messageBySession.get( sessionId )
    sessionMap.values().toIndexedSeq.foreach( 
      // clean out old data
      (scan) => if ( scan.dateCreated.isBefore( minAge ) ) {
        sessionMap.remove( scan.handle.id )
      }
    )
    sessionMap.put( event.handle.id, event )
    listeners.foreach(
      (listener) => {
        exec.submit( new Runnable(){
            override def run():Unit = try {
              log.log( Level.FINE, ">>>> Event bus handler processing event: " + event.handle )
              val fbBuilder = fbFactory.get
              val fbListener = fbListenerFactory.get.processor(
                SimpleMessageProcessor.this 
              ).handle( event.handle 
              ).build
              fbBuilder.addLittleListener( fbListener )
              fbBuilder.addPropertyChangeListener( fbListener )
              listener.messageArrival( event, fbBuilder.build )
              postResponse( event.handle, 
                           responseFactory.get.state( model.Response.State.COMPLETE ).progress( 100 ).build 
              )     
            } catch {
              case ex => {
                  log.log( Level.WARNING, "Event dispatch failed", ex )
                  postResponse( event.handle, 
                               responseFactory.get.state( model.Response.State.FAILED 
                    ).progress( 100 
                    ).addFeedback( ex.toString
                    ).build 
                  )
                }
            } 
          }
        )
      } //processor.eventBus.post( EventListenerPair( event, listener ) )
    )
    
  }

     
  val responseByMessage:gcache.LoadingCache[UUID,SimpleMessageProcessor.ResponseQueue] =
    gcache.CacheBuilder.newBuilder(
    ).expireAfterWrite( 3, java.util.concurrent.TimeUnit.MINUTES 
    ).build(
      new gcache.CacheLoader[UUID,SimpleMessageProcessor.ResponseQueue] {
        override def load( key:UUID ):SimpleMessageProcessor.ResponseQueue = new SimpleMessageProcessor.ResponseQueue
      }
    )
  
  override def postResponse( handle:model.MessageHandle, response:model.Response ):Unit = {
    val envelope = model.ResponseEnvelope( 0L, jtime.DateTime.now, handle, response )
    responseByMessage.get( handle.id ).push( envelope )
  }

}

object SimpleMessageProcessor {
  /**
   * Internal utility class to manage concurrent queue of response messages
   */
  class ResponseQueue {
    private var qBuilder = Seq.newBuilder[model.ResponseEnvelope]
    
    def push( envelope:model.ResponseEnvelope ):ResponseQueue = this.synchronized {
      qBuilder += envelope
      this
    }
    
    def popAll():Seq[model.ResponseEnvelope] = this.synchronized {
      val result = qBuilder.result
      qBuilder = Seq.newBuilder[model.ResponseEnvelope]
      result
    }
  }
  
  class SMPClient @inject.Inject() ( 
    processor:SimpleMessageProcessor,
    loginStrategy:LoginStrategy
  ) extends remote.MessageRemote {
    /**
     * Just a simple pass-through login
     */
    def login( creds:model.Credentials ):model.ClientSession = loginStrategy.login( creds )

    
    def postMessage( client:model.ClientSession, msg:model.Message ):model.MessageHandle = {
      require( client.dateExpires.isAfter( jtime.DateTime.now ), "session expired: " + client )
      val now = jtime.DateTime.now
      val event = model.MessageEvent( msg, 
                                     new model.internal.SimpleMessageHandle( java.util.UUID.randomUUID ),
                                     client,
                                     now
      )
      processor.postClientMessage(event)
      /*
      val listeners:Seq[MessageListener] = processor.getListeners( msg.messageType )
      require( ! listeners.isEmpty, "Listeners registers for posted message type: " + msg.messageType )
      listeners.foreach(
        (listener) => processor.eventBus.post( EventListenerPair( event, listener ) )
      )
      val sessionId = event.session.sessionId
      val minAge = now.minusMinutes(6)
      messageBySession.synchronized {
        messageBySession.put( sessionId, 
                             Option( messageBySession.getIfPresent( sessionId ) ).toSeq.flatten.filter( _.dateCreated.isAfter( minAge )) :+ event
        )
      }
      */
      event.handle
    }
    
    
    def checkResponse( client:model.ClientSession, handle:model.MessageHandle ):Seq[model.ResponseEnvelope] = 
      processor.responseByMessage.get( handle.id ).popAll
    
    def checkResponse( client:model.ClientSession ):Map[UUID,Seq[model.ResponseEnvelope]] = {
      val eventSeq:Seq[model.MessageEvent] = processor.messageBySession.get( client.sessionId ).values.toList
      eventSeq.map( _.handle ).distinct.map( (handle) => handle.id -> checkResponse( client, handle ) ).toMap
    }
  }
  
  object SMPClient {
    /** Guice provider */
    class Provider @inject.Inject() ( processor:SimpleMessageProcessor ) extends inject.Provider[remote.MessageRemote] {
      private lazy val singleton = processor.client
      override def get():remote.MessageRemote = singleton
    }
  }
  //-------------------------------------------
  
  case class EventListenerPair( event:model.MessageEvent, listener:MessageListener ) {}

  
}