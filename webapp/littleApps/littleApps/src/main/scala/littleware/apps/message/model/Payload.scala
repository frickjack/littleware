/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.message.model

import com.google.gson


/**
 * Little trait for message/response instances to help
 * the message engine serialize the response to JSON
 * and possibly other formats in the future.
 * Payload's Gson type adapter invokes toJson, so client
 * code can invoke gsonTool.toJson( payloadObject, classOf[Payload] )
 */
trait Payload {
  val payloadType:String
}

object Payload {
  trait GsonAdapter extends gson.JsonSerializer[Payload] with gson.JsonDeserializer[Payload] {}
  
  /**
    * Registry for different types of picklers (currently just json).
    * Different Payload providers should register serializers at
    * application startup time via a littleware module.
    */
  trait PickleRegistry {
    def jsonSerializer( payloadType:String ):Option[gson.JsonSerializer[Payload]]
    def jsonDeserializer( payloadType:String ):Option[gson.JsonDeserializer[Payload]]
    /**
      * Throws an error if same type registered more than once
      */
    def register( payloadType:String, handler:GsonAdapter ):Unit
  }
  
  /**
   * Convenience base class for Payload JSON adapter implementations
   */
  abstract class AbstractGsonAdapter extends GsonAdapter {
    /**
     * Base implementation just uses default gson serialization
     */
    override def serialize( src:Payload,
                          srcType:java.lang.reflect.Type,
                          ctx:gson.JsonSerializationContext
    ):gson.JsonElement = ctx.serialize( src )
    
    
    /**
     * Base implementation throws UnsupportedOperationException
     */
    override def deserialize( jsonIn:gson.JsonElement,
                            destType:java.lang.reflect.Type,
                            ctx:gson.JsonDeserializationContext
    ):Payload = {
      throw new UnsupportedOperationException( "not yet implemented" )
    }      
  }
}
