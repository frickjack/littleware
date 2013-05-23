/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.littleId
package gsonAdapter

import com.google.gson

import com.google.inject
import littleware.base.UUIDFactory
import org.joda.{time => jtime}

import server.model.AuthRequest



class AuthRequestAdapter @inject.Inject() ( requestFactory:inject.Provider[AuthRequest.Builder] 
  ) extends gson.JsonSerializer[AuthRequest] with gson.JsonDeserializer[AuthRequest] {
    
  def serialize( src:AuthRequest,
                typeOfSrc:java.lang.reflect.Type,
                context:gson.JsonSerializationContext 
  ):gson.JsonElement = {
    val js = new gson.JsonObject
    js.addProperty( "id", src.id.toString )
    js.addProperty( "provider", src.openIdProvider.toString )
    js.addProperty( "dateTime", src.dateTime.toString() )
    js
  }


  def deserialize( je:gson.JsonElement,  typeOut:java.lang.reflect.Type, 
                  jdc:gson.JsonDeserializationContext ):AuthRequest = {
    val js = je.getAsJsonObject
    requestFactory.get.id( UUIDFactory.parseUUID( js.get( "id" ).getAsString() )
      ).openIdProvider( common.model.OIdProvider.withName( js.get( "provider" ).getAsString() )
      ).dateTime( jtime.DateTime.parse( js.get( "dateTime" ).getAsString() ) 
      ).build()
  }

}