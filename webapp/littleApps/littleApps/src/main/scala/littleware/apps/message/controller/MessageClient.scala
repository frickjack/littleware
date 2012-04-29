/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.message
package controller

trait MessageClient {
  def login( creds:model.Credentials ):model.ClientSession
  def postMessage( client:model.ClientSession, msg:model.Message ):model.MessageHandle
  def checkResponse( client:model.ClientSession, handle:model.MessageHandle ):Seq[model.ResponseEnvelope]
  def checkResponse( client:model.ClientSession ):Seq[model.ResponseEnvelope]
}
