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
import java.util.logging.{Level,Logger}


class GsonPayloadAdapter @inject.Inject() (
  registry:Payload.PickleRegistry
) extends gson.JsonSerializer[Payload] with gson.JsonDeserializer[Payload] {
  private val log = Logger.getLogger( getClass.getName )
  
  
  override def serialize( src:Payload,
                         srcType:java.lang.reflect.Type,
                         ctx:gson.JsonSerializationContext
  ):gson.JsonObject = {
    val picklerLookup:Option[gson.JsonSerializer[Payload]] = registry.jsonSerializer(src.payloadType)
    val content:gson.JsonElement = picklerLookup.map( 
      _.serialize( src, classOf[Payload], ctx ) 
    ).getOrElse( src match {
        case jsp:JsonPayload => jsp.payload
        case _ => {
            log.log( Level.WARNING, "No json adapter register for payload type, using default gson serializer: " + src.payloadType )
            ctx.serialize( src )
          }
    })

    val json = new gson.JsonObject
    json.addProperty( "payloadType", src.payloadType )
    json.add( "content", content )
    json
  }
  
  
  override def deserialize( jsonIn:gson.JsonElement,
                           destType:java.lang.reflect.Type,
                           ctx:gson.JsonDeserializationContext
  ):Payload = {
    val json = jsonIn.getAsJsonObject
    val payloadType = json.get( "payloadType" ).getAsString
    val picklerLookup = registry.jsonDeserializer(payloadType)
    picklerLookup.map( 
      _.deserialize( json.get( "content" ), classOf[Payload], ctx )
    ).getOrElse(
      {
        log.log( Level.WARNING, "No json deserializer registered for payload type, using JsonPayload fallback: " + payloadType )
        JsonPayload( payloadType, json.get( "content" ) )
      }
    )
  }

}
