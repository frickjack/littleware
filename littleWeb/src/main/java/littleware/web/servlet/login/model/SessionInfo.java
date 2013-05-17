/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.web.servlet.login.model;

import java.util.UUID;
import littleware.base.Option;
import littleware.security.LittleUser;
import littleware.web.beans.GuiceBean;

/**
 * Little container for some session variables. This
 * thing is not a pojo with set-once properties - it's
 * more of a state machine whose state may change as
 * the state of the underlying guice session changes
 * via authentication or whatever.
 * SessionInfo is a singleton per session.
 */
public interface SessionInfo {

  /** Get the session id associated (equals the guice session id) */
  UUID getId();

  /**
   * Get the guice bean associated with this session
   */
  GuiceBean getGBean();
  
  /**
   * Return the currently logged in user if any.
   * Note that the return value may change over the lifetime of
   * the session from empty to whoever is authenticated.
   */
  Option<LittleUser> getActiveUser();

  /**
   * Return the credentials necessary to identify and authenticate
   * this session.
   * Note that the return value may change over the lifetime of
   * the session from empty to whoever is authenticated.
   * 
   * @return credentials valid at current point in time
   */
  SessionCreds getCredentials();
}
