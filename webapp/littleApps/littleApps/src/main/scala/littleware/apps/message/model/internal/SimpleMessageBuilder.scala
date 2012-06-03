/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.message.model
package internal


class SimpleMessageBuilder extends Message.Builder {
  override def build():Message = {
    assert( null != messageType, "null messageType" )
    assert( null != payload, "null payload" )
    SimpleMessageBuilder.SimpleMessage( messageType, payload )
  }
}

object SimpleMessageBuilder {
  
  case class SimpleMessage(
    messageType:String,
    payload:Payload    
  ) extends Message {}
  
}
