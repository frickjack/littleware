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
import scala.collection.JavaConversions._


import server.model.{AuthRequest,DataForProvider}


class DataForProviderAdapter extends gson.JsonSerializer[DataForProvider] with gson.JsonDeserializer[DataForProvider] {
  def serialize( src:DataForProvider,
                typeOfSrc:java.lang.reflect.Type,
                context:gson.JsonSerializationContext 
  ):gson.JsonElement = {
    val js = new gson.JsonObject
    js.add( "request", context.serialize( src.request, classOf[AuthRequest] ))
    js.addProperty( "endpoint", src.providerEndpoint.toString )
    js.add( "params", {
        val js = new gson.JsonObject
        src.params.foreach( { case (key,value) => js.addProperty( key, value )})
        js
      })
    js 
  }


  def deserialize( je:gson.JsonElement,  typeOut:java.lang.reflect.Type, 
                  jdc:gson.JsonDeserializationContext ):DataForProvider = {
    val js = je.getAsJsonObject
    DataForProvider(
      jdc.deserialize( js.get( "request" ), classOf[AuthRequest] ),
      new java.net.URL( js.get( "endpoint" ).getAsString ),
      js.get( "params" ).getAsJsonObject.entrySet.map( ent => ent.getKey -> ent.getValue.getAsString ).toMap
    )
  }

}