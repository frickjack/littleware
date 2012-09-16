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
 * injected executor service and feedback factory
 */
class SimpleMessageProcessor @inject.Inject()(
  exec:ListeningExecutorService,
  fbFactory:inject.Provider[feedback.Feedback.Builder],
  responseFactory:inject.Provider[model.Response.Builder],
  fbListenerFactory:inject.Provider[FeedbackListener.Builder]
) extends MessageProcessor {
  private val log = Logger.getLogger( getClass.getName )
  
  private lazy val _client:remote.MessageRemote = new SimpleMessageProcessor.SMPClient( this )
  override def client:remote.MessageRemote = _client
  
  //val WILDCARD:String = "*"
  private[internal] val type2Listener:java.util.concurrent.ConcurrentMap[String,MessageListener] = 
    new gcollect.MapMaker().concurrencyLevel(4).makeMap()
  
  //private[internal] val id2Listener:java.util.concurrent.ConcurrentMap[UUID,MessageListener] = new gcollect.MapMaker().makeMap[UUID,MessageListener]()
  
  def getListeners( typeSpec:String ):Seq[MessageListener] = Option( type2Listener.get( typeSpec ) ).toSeq
  //(type2Listener.get( WILDCARD ) ++ type2Listener.get( typeSpec )).flatMap( (id) => Option( id2Listener.get(id) ) ).toSeq

  
  /**
   * Event bus on which to push client messages for eventual processing
   */
  val eventBus:gbus.AsyncEventBus = new gbus.AsyncEventBus( "SimpleMessageProcessor", exec )
  
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
    val listeners:Seq[MessageListener] = getListeners( event.message.messageType )
    require( ! listeners.isEmpty, "Listeners registers for posted message type: " + event.message.messageType )
    val sessionId = event.session.sessionId
    val minAge = jtime.DateTime.now.minusMinutes(6)
    val sessionMap:java.util.Map[UUID,model.MessageEvent] = messageBySession.get( sessionId )
    sessionMap.values().toIndexedSeq[model.MessageEvent].foreach( 
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

  // dead code ... doesn't work - WTF ?
  eventBus.register(
    /**
     * Event bus listener dispatches events to registered listeners
     * with a feedback instance that has listeners attached that
     * auto-generate intermediate responses.
     */
    new java.lang.Object() {
      @gbus.Subscribe()
      def handler( elPair:SimpleMessageProcessor.EventListenerPair ):Unit = try {
        log.log( Level.FINE, ">>>> Event bus handler processing event: " + elPair.event.handle )
        val fbBuilder = fbFactory.get
        val fbListener = fbListenerFactory.get.processor( SimpleMessageProcessor.this ).handle( elPair.event.handle ).build
        fbBuilder.addLittleListener( fbListener )
        fbBuilder.addPropertyChangeListener( fbListener )
        elPair.listener.messageArrival( elPair.event, fbBuilder.build )
        postResponse( elPair.event.handle, 
                     responseFactory.get.state( model.Response.State.COMPLETE ).progress( 100 ).build 
        )     
      } catch {
        case ex => {
            log.log( Level.WARNING, "Event dispatch failed", ex )
            postResponse( elPair.event.handle, 
                         responseFactory.get.state( model.Response.State.FAILED 
              ).progress( 100 
              ).addFeedback( ex.toString
              ).build 
            )
          }
      } 
    }
  )
  
  override def setListener( typeSpec:String, listener:MessageListener ):Unit = {
    require( typeSpec.matches( "^[\\w\\.-]+" ), 
            "Valid listener type spec: " + typeSpec 
    )
    type2Listener.put( typeSpec, listener )
    //id2Listener.put( listener.id, listener )
  }
  
  /*
   override def addListener( listener:MessageListener ):Unit = {
   type2Listener.put( WILDCARD, listener.id )
   id2Listener.put( listener.id, listener )
   }
  
   override def removeListener( typeSpec:String, id:UUID ):Unit = {
   require( typeSpec.matches( "^\\w+" ), "Valid listener type spec: " + typeSpec )
   type2Listener.remove( typeSpec, id )
   if ( ! type2Listener.containsKey( id ) ) {
   id2Listener.remove( id )
   }
   }
  
   override def removeListener( id:UUID ):Unit = {
   type2Listener.keySet.foreach( (k) => type2Listener.remove( k, id ) )
   id2Listener.remove( id )
   }
   */
   
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
  
  class SMPClient @inject.Inject() ( processor:SimpleMessageProcessor ) extends remote.MessageRemote {
    /**
     * Just a simple pass-through login
     */
    def login( creds:model.Credentials ):model.ClientSession = {
      val now = jtime.DateTime.now
      model.internal.NullClientSession( java.util.UUID.randomUUID, now, now.plusDays(100) )
    }

    
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