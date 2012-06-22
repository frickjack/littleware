/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.message
package client

/**
 * MessageClient decorator manages ClientSession,
 * so code doesn't have to pass that around
 */
import java.util.UUID

trait SessionMessageClient {
  val session:model.ClientSession
  
  def postMessage( msg:model.Message ):model.MessageHandle
  def checkResponse( handle:model.MessageHandle ):Seq[model.ResponseEnvelope]
  def checkResponse():Map[UUID,Seq[model.ResponseEnvelope]]
  
}
