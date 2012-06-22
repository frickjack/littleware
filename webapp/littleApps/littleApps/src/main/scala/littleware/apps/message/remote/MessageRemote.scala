/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.message
package remote

import java.util.UUID

trait MessageRemote {
  def login( creds:model.Credentials ):model.ClientSession
  def postMessage( client:model.ClientSession, msg:model.Message ):model.MessageHandle
  
  /**
   * Check for responses to the message with the given handle
   */
  def checkResponse( client:model.ClientSession, handle:model.MessageHandle ):Seq[model.ResponseEnvelope]
  /**
   * Check for outstanding responses to any message sent by this client
   * 
   * @return map from message-handle id to response sequence
   */
  def checkResponse( client:model.ClientSession ):Map[UUID,Seq[model.ResponseEnvelope]]
}

