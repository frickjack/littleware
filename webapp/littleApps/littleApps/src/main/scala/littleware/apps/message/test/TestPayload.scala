/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.message
package test

import com.google.gson

/**
 * Simple message-string payload
 */
case class TestPayload( val message:String ) extends model.Payload {
  val payloadType:String = TestPayload.payloadType
}

object TestPayload {
  val payloadType = classOf[TestPayload].getName 
  
  /**
   * Startup module littleware.message.LittleAppModule registers
   * this adapter with the Payload pickle registry.
   */
  object GsonAdapter extends model.Payload.GsonAdapter {
    
    override def serialize( src:model.Payload,
                          srcType:java.lang.reflect.Type,
                          ctx:gson.JsonSerializationContext
    ):gson.JsonObject = {
      val js = new gson.JsonObject
      val payload = src.asInstanceOf[TestPayload]
      js.addProperty( "message", payload.message )
      js
    }    
    
    override def deserialize( jsonIn:gson.JsonElement,
                            destType:java.lang.reflect.Type,
                            ctx:gson.JsonDeserializationContext
    ):model.Payload = TestPayload( jsonIn.getAsJsonObject.get( "message" ).getAsString )
  }
  
}



