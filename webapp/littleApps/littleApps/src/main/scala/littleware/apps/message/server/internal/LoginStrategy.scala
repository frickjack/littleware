/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.message
package server
package internal

/**
 * Interface for strategy to which a server-side RemoteLoginManager may
 * deligate authentication of credentials.
 */
trait LoginStrategy {
  /**
   * Returns true if this strategy can handle the given credentials
   */
  def acceptsCreds( creds:model.Credentials ):Boolean
  
  /**
   * @throws IllegalArgumentException if ! acceptsCreds( creds )
   * @throws LoginException if credentials fail authentication
   */
  def login( creds:model.Credentials ):model.ClientSession
}
