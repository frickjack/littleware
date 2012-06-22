/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.message
package remote


/**
 * Specialization of MessageRemote for REST web client implementation
 */
trait WebMessageRemote extends MessageRemote {
  val serviceRoot:java.net.URL
}

object WebMessageRemote {
  trait Builder {
    var serviceRoot:java.net.URL = new java.net.URL( "http://localhost:1238/littleware/services/message" )
    def serviceRoot( value:java.net.URL ):this.type = {
      serviceRoot = value
      this
    }
    
    def build():WebMessageRemote
  }
}
