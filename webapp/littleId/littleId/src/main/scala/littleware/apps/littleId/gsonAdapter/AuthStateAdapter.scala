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

import server.model.AuthState


class AuthStateAdapter extends gson.JsonSerializer[AuthState] with gson.JsonDeserializer[AuthState] {
  def serialize( src:AuthState,
                typeOfSrc:java.lang.reflect.Type,
                context:gson.JsonSerializationContext 
  ):gson.JsonElement = {
    throw new UnsupportedOperationException( "not yet implemented" )          
  }


  def deserialize( je:gson.JsonElement,  typeOut:java.lang.reflect.Type, 
                  jdc:gson.JsonDeserializationContext ):AuthState = {
    throw new UnsupportedOperationException( "not yet implemented" )
  }

}