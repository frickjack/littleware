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
 * Payload implementation with a gson.JsonElement payload.
 * This is the fallback implementation for deserialization
 * when a json adapter is not registered with the Payload pickle registry.
 */
case class JsonPayload( 
  payloadType:String,
  payload:gson.JsonElement 
) extends Payload {}

