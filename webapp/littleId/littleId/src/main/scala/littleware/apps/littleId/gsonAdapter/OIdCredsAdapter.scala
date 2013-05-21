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
import scala.collection.JavaConversions._

import common.model.OIdUserCreds


class OIdCredsAdapter @inject.Inject() (
  credsFactory:inject.Provider[OIdUserCreds.Builder]
)  extends gson.JsonSerializer[OIdUserCreds] with gson.JsonDeserializer[OIdUserCreds] {
  def serialize( src:OIdUserCreds,
                typeOfSrc:java.lang.reflect.Type,
                context:gson.JsonSerializationContext 
  ):gson.JsonElement = {
    val js = new gson.JsonObject
              js.addProperty( "name", src.name )
              js.addProperty( "email", src.email )
              js.addProperty( "openId", src.openId.toString )
    js 
  }


  def deserialize( je:gson.JsonElement,  typeOut:java.lang.reflect.Type, 
                  jdc:gson.JsonDeserializationContext ):OIdUserCreds = {
    val js = je.getAsJsonObject
    credsFactory.get.email( js.get( "email" ).getAsString
             ).openId( new java.net.URL( js.get( "openId" ).getAsString ) 
             ).build
  }

}