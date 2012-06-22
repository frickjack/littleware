/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.message.model
package internal

import com.google.gson
import com.google.inject
import littleware.base.UUIDFactory
import org.joda.{time => jtime}
import scala.collection.JavaConversions._

class GsonEnvelopeAdapter @inject.Inject() ( responseFactory:inject.Provider[Response.Builder]) (
) extends gson.JsonSerializer[ResponseEnvelope] with gson.JsonDeserializer[ResponseEnvelope] {

  override def serialize( src:ResponseEnvelope,
                         srcType:java.lang.reflect.Type,
                         ctx:gson.JsonSerializationContext
  ):gson.JsonObject = {
    val jsResult = new gson.JsonObject
    jsResult.addProperty( "transaction", src.transaction )
    jsResult.addProperty( "datePosted", src.datePosted.toString() )
    jsResult.addProperty( "handle", src.handle.id.toString )
    val jsResp = new gson.JsonObject
    jsResp.addProperty( "progress", src.response.progress )
    jsResp.addProperty( "state", src.response.state.toString )
    jsResp.add( "feedback", {
        val js = new gson.JsonArray
        src.response.feedback.foreach( (s) => js.add( new gson.JsonPrimitive( s )))
        js
      }
    )
    jsResp.add( "results", {
        val js = new gson.JsonArray
        src.response.results.foreach( (payload) => js.add( ctx.serialize( payload, classOf[Payload]) ))
        js
      }
    )
    jsResult.add( "response", jsResp )
    jsResult
  }
  
  
  override def deserialize( jsonIn:gson.JsonElement,
                           destType:java.lang.reflect.Type,
                           ctx:gson.JsonDeserializationContext
  ):ResponseEnvelope = {
    val json = jsonIn.getAsJsonObject
    val jsResponse = json.get( "response" ).getAsJsonObject
    val responseBuilder = responseFactory.get.progress( jsResponse.get( "progress" ).getAsInt 
    ).state( Response.State.withName( jsResponse.get( "state" ).getAsString ) )
    jsResponse.get( "feedback" ).getAsJsonArray.iterator.foreach( 
      (x) => responseBuilder.addFeedback( x.getAsString )
    )
    jsResponse.get( "results" ).getAsJsonArray.iterator.foreach(
      (x) => responseBuilder.addResult( ctx.deserialize( x, classOf[Payload]))
    )
    ResponseEnvelope(
      json.get( "transaction" ).getAsLong,
      jtime.DateTime.parse( json.get( "datePosted" ).getAsString ),
      MessageHandle( UUIDFactory.parseUUID( json.get( "handle" ).getAsString )),
      responseBuilder.build
    )
  }
  
}
