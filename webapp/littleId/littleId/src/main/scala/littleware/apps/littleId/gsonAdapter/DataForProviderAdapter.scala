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

import server.model.DataForProvider


class DataForProviderAdapter extends gson.JsonSerializer[DataForProvider] with gson.JsonDeserializer[DataForProvider] {
  def serialize( src:DataForProvider,
                typeOfSrc:java.lang.reflect.Type,
                context:gson.JsonSerializationContext 
  ):gson.JsonElement = {
    throw new UnsupportedOperationException( "not yet implemented" )          
  }


  def deserialize( je:gson.JsonElement,  typeOut:java.lang.reflect.Type, 
                  jdc:gson.JsonDeserializationContext ):DataForProvider = {
    throw new UnsupportedOperationException( "not yet implemented" )
  }

}