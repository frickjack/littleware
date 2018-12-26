package littleware.apps.message.model
package internal

import com.google.gson
import com.google.inject

class GsonMessageAdapter @inject.Inject() (
  msgFactory:inject.Provider[Message.Builder]
  ) extends gson.JsonSerializer[Message] with gson.JsonDeserializer[Message] {

  override def serialize( src:Message,
                         srcType:java.lang.reflect.Type,
                         ctx:gson.JsonSerializationContext
  ):gson.JsonObject = {
    val js = new gson.JsonObject
    js.addProperty( "messageType", src.messageType )
    js.add( "payload", ctx.serialize( src.payload, classOf[Payload]))
    js
  }
  
  
  override def deserialize( jsonIn:gson.JsonElement,
                           destType:java.lang.reflect.Type,
                           ctx:gson.JsonDeserializationContext
  ):Message = {
    val js = jsonIn.getAsJsonObject
    msgFactory.get.messageType( js.get( "messageType" ).getAsString
    ).payload( ctx.deserialize( js.get( "payload" ), classOf[Payload] )
    ).build
  }
}
