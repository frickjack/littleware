/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.message.model


trait Message {
  /**
   * messageType must be alpha numeric
   */
  val messageType:String
  val payload:Payload
}

object Message {
  /**
   * Builder for generic Message.
   * This is the mechanism that the MessageClientServlet
   * uses to deserialize messages posted via the REST API.
   */
  trait Builder {
    var messageType:String = null
    def messageType( value:String ):this.type = {
      messageType = value
      this
    }
    
    var payload:Payload = null
    def payload( value:Payload ):this.type = {
      payload = value
      this
    }
    
    def build():Message
  }
  
}
