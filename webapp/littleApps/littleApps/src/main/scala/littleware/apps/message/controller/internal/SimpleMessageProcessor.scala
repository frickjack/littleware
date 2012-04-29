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
  responseFactory:inject.Provider[model.Response.Builder]
) extends MessageProcessor {
  private val log = Logger.getLogger( getClass.getName )
  
  private lazy val _client:MessageClient = new SimpleMessageProcessor.SMPClient( this )
  override def client:MessageClient = _client
  
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

  
  eventBus.register(
    /**
     * Event bus listener dispatches events to registered listeners
     * with a feedback instance that has listeners attached that
     * auto-generate intermediate responses.
     */
    new java.lang.Object() {
      @gbus.Subscribe()
      def handler( elPair:SimpleMessageProcessor.EventListenerPair ):Unit = try {
        val fbBuilder = fbFactory.get
        val fbListener = new FeedbackListener( 
          SimpleMessageProcessor.this, elPair.event.handle,
          responseFactory 
        )
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
    require( typeSpec.matches( "^\\w+" ), "Valid listener type spec: " + typeSpec )
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
  val responseByMessage:gcache.Cache[UUID,Seq[model.ResponseEnvelope]] =
    gcache.CacheBuilder.newBuilder(
    ).expireAfterWrite( 3, java.util.concurrent.TimeUnit.MINUTES 
    ).build()
  
  override def postResponse( handle:model.MessageHandle, response:model.Response ):Unit = {
    val envelope = model.ResponseEnvelope( 0L, jtime.DateTime.now, handle, response )
    responseByMessage.synchronized {
      val responseSeq:Seq[model.ResponseEnvelope] = 
        Option( responseByMessage.getIfPresent( handle.id ) ).toSeq.flatten :+ envelope
    
      responseByMessage.put( handle.id, responseSeq )
    }
  }

}

object SimpleMessageProcessor {
  class SMPClient @inject.Inject() ( processor:SimpleMessageProcessor ) extends MessageClient {
    /**
     * Just a simple pass-through login
     */
    def login( creds:model.Credentials ):model.ClientSession = {
      val now = jtime.DateTime.now
      model.internal.NullClientSession( java.util.UUID.randomUUID, now, now.plusDays(100) )
    }

    val messageBySession:gcache.Cache[UUID,Seq[model.MessageHandle]] =
      gcache.CacheBuilder.newBuilder(
      ).expireAfterAccess( 5, java.util.concurrent.TimeUnit.MINUTES 
      ).build()
    
    def postMessage( client:model.ClientSession, msg:model.Message ):model.MessageHandle = {
      require( client.dateExpires.isAfter( jtime.DateTime.now ), "session expired: " + client )
      val event = model.MessageEvent( msg, 
                                     new model.internal.SimpleMessageHandle( java.util.UUID.randomUUID ),
                                     client
      )
      processor.getListeners( msg.messageType ).foreach(
        (listener) => processor.eventBus.post( EventListenerPair( event, listener ) )
      )
      val sessionId = event.session.sessionId
      messageBySession.synchronized {
        messageBySession.put( sessionId, 
                             Option( messageBySession.getIfPresent( sessionId ) ).toSeq.flatten :+ event.handle
        )
      }
      
      event.handle
    }
    
    private lazy val responseByMessage:java.util.concurrent.ConcurrentMap[UUID,Seq[model.ResponseEnvelope]] = 
      processor.responseByMessage.asMap
    
    def checkResponse( client:model.ClientSession, handle:model.MessageHandle ):Seq[model.ResponseEnvelope] = 
      Option( responseByMessage.remove( handle.id ) ).toSeq.flatten
    
    def checkResponse( client:model.ClientSession ):Seq[model.ResponseEnvelope] = throw new UnsupportedOperationException( "Not yet implemented" )
  }
  
  object SMPClient {
    /** Guice provider */
    class Provider @inject.Inject() ( processor:SimpleMessageProcessor ) extends inject.Provider[MessageClient] {
      private lazy val singleton = new SMPClient( processor )
      override def get():MessageClient = singleton
    }
  }
  //-------------------------------------------
  
  case class EventListenerPair( event:model.MessageEvent, listener:MessageListener ) {}

  
}