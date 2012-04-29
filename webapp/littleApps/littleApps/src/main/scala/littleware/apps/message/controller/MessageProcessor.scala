/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.message
package controller

trait MessageProcessor {
  /**
   * A client that posts messages to this processor
   */
  def client:MessageClient
  
  /**
   * Set the listener for messages of the given type
   */
  def setListener( typeSpec:String, listener:MessageListener ):Unit
  /*
  /**
   * Add a listener for every type of message
   */
  def addListener( listener:MessageListener ):Unit
  def removeListener( typeSpec:String, id:java.util.UUID ):Unit
  def removeListener( id:java.util.UUID ):Unit
  */
  def postResponse( handle:model.MessageHandle, response:model.Response ):Unit
}
