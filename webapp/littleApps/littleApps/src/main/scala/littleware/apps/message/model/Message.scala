/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.message.model

import com.google.gson

trait Message {
  /**
   * messageType must be alpha numeric
   */
  val messageType:String
  val payload:gson.JsonObject
}

