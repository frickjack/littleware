/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.message
package server

trait MessageProcessor {
  /**
   * A client that posts messages to this processor
   */
  def client:remote.MessageRemote
  
  /**
   * Set the listener class for messages of the given type.
   * When a message of the specified type arrives, a session-scoped
   * instance of each registered listener for that type will be injected
   * and invoked.
   */
  def setListener( typeSpec:String, listenerClass:Class[_ <: MessageListener] ):Unit
  
 
  def postResponse( handle:model.MessageHandle, response:model.Response ):Unit
}
