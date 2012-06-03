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
import com.google.common.{collect => gcollect}

object SimplePayloadPickleRegistry extends Payload.PickleRegistry {
  private val registry = new gcollect.MapMaker().concurrencyLevel(4).makeMap[String,Payload.GsonAdapter]
  
  def jsonSerializer( payloadType:String ):Option[gson.JsonSerializer[Payload]] =
    Option( registry.get( payloadType ) )
    
  def jsonDeserializer( payloadType:String ):Option[gson.JsonDeserializer[Payload]] =
    Option( registry.get( payloadType ) )
    
  def register( payloadType:String, handler:Payload.GsonAdapter ):Unit =
    registry.put( payloadType, handler )
    
}
