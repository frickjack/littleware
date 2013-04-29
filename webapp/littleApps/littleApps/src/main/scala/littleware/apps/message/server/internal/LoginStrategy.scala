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

import littleware.bootstrap

/**
 * Interface for strategy to which a server-side RemoteLoginManager may
 * deligate authentication of credentials.
 */
trait LoginStrategy {  
  /**
   * Creates a new session, authenticate, and cache session injector
   * for later access by lookup()
   * 
   * @return authenticaed session - use strategy.lookup()
   * @throws IllegalArgumentException if ! acceptsCreds( creds )
   * @throws LoginException if credentials fail authentication
   */
  def login( creds:model.Credentials ):model.ClientSession
  
  /**
   * Lookup the session injector associated with the given id.
   * Should succeed for all valid session ids.
   * Different implementations may access session data from
   * a database or whatever.
   */
  def lookup( sessionId:java.util.UUID ):bootstrap.SessionInjector

}
