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

import common.model.OIdUserCreds
import server.model.{AuthRequest,AuthState}
import AuthState._


class AuthStateAdapter @inject.Inject() (
  credsFactory:inject.Provider[OIdUserCreds.Builder]
) extends gson.JsonSerializer[AuthState] with gson.JsonDeserializer[AuthState] {
  
  def serialize( src:AuthState,
                typeOfSrc:java.lang.reflect.Type,
                context:gson.JsonSerializationContext 
  ):gson.JsonElement = {
    val js = new gson.JsonObject
    js.add( "request", context.serialize( src.request, classOf[server.model.AuthRequest]))
    src match {
      case Success( _, creds, secret ) => {
          js.addProperty( "state", "success" )
          js.add( "creds", context.serialize( creds, classOf[OIdUserCreds] ) )
          js.addProperty( "token", secret )
          js
      }
      case _:Failure => js.addProperty( "state", "failure" )
      case _:Running => js.addProperty( "state", "running" )
    }
    js
  }


  def deserialize( je:gson.JsonElement,  typeOut:java.lang.reflect.Type, 
                  jdc:gson.JsonDeserializationContext ):AuthState = {
    val js = je.getAsJsonObject
    val request:AuthRequest = jdc.deserialize( js.get( "request" ), classOf[AuthRequest] )
    js.get( "state" ).getAsString match {
      case "success" => Success( request, 
                jdc.deserialize( js.get( "creds" ), classOf[OIdUserCreds] ), 
                js.get( "token" ).getAsString 
        )
      case "failure" => Failure( request )
      case "running" => Running( request )
    }
  }

}