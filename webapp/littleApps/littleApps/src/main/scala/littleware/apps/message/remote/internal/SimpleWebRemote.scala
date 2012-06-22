/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.message
package remote
package internal

import com.google.gson
import com.google.inject
import java.util.UUID
import littleware.asset.client.internal.HttpHelper
import littleware.base.Whatever
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.{HttpGet,HttpPut}
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.BasicResponseHandler
import org.joda.{time => jtime}
import scala.collection.JavaConversions._

/**
 * Simple HTTP API client
 */
class SimpleWebRemote ( 
  val serviceRoot:java.net.URL,
  httpHelper:HttpHelper, 
  httpClient:HttpClient,
  gsonFactory:inject.Provider[gson.Gson]
) extends WebMessageRemote {
  private lazy val gsonTool = gsonFactory.get
  
  
  def login( creds:model.Credentials ):model.ClientSession = {
    val now = jtime.DateTime.now()
    model.internal.NullClientSession( java.util.UUID.randomUUID, now, now.plusDays(1) )
  }
  
  def postMessage( client:model.ClientSession, msg:model.Message ):model.MessageHandle = {
    val putMethod = new HttpPut(serviceRoot.toString().replaceAll( "/+$", "" ) + "/" + java.util.UUID.randomUUID)
    putMethod.setEntity( new StringEntity( gsonTool.toJson( msg, classOf[model.Message] ), "UTF-8" ) )
    val response:String = httpClient.execute( putMethod, new BasicResponseHandler() )
    val js = gsonTool.fromJson( response, classOf[gson.JsonElement] ).getAsJsonObject
    Option( js.get( "handle" ) ).map( 
      (jsHandle) => littleware.base.UUIDFactory.parseUUID( jsHandle.getAsString )
    ).filter( (_) => js.get( "status" ).getAsString.toLowerCase == "ok" 
    ).map( model.MessageHandle(_) 
    ).getOrElse(
      throw new IllegalStateException( "Post failed: " + js.get( "description" ).getAsString )
    )
  }
  
  def checkResponse( client:model.ClientSession, handle:model.MessageHandle ):Seq[model.ResponseEnvelope] = {
    val getMethod = new HttpGet( serviceRoot.toString().replaceAll( "/+$", "" ) + "/" + handle.id )
    val response:String = httpClient.execute( getMethod, new BasicResponseHandler() )
    val jsResponse = gsonTool.fromJson( response, classOf[gson.JsonElement] ).getAsJsonObject
    Option( jsResponse.get( "envelopes" ) ).toSeq.flatMap( (js) => {
        val jsEnvelopes = js.getAsJsonArray 
        jsEnvelopes.iterator.map( (jsEnv) => {
            gsonTool.fromJson( jsEnv, classOf[model.ResponseEnvelope] )
          }
        ).toSeq
    } )
  }
  
  def checkResponse( client:model.ClientSession ):Map[UUID,Seq[model.ResponseEnvelope]] =
    throw new UnsupportedOperationException( "not yet implemented" )
  
}

object SimpleWebRemote {
  class Builder @inject.Inject()(
      httpHelper:HttpHelper, 
      httpClient:HttpClient,
      gsonFactory:inject.Provider[gson.Gson]    
    ) extends WebMessageRemote.Builder with inject.Provider[WebMessageRemote] {
    
    def build():SimpleWebRemote = {
      new SimpleWebRemote( serviceRoot, httpHelper, httpClient, gsonFactory )
    }
    
    def get():SimpleWebRemote = build()
  }
}