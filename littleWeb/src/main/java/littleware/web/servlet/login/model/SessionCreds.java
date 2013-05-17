/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.web.servlet.login.model;

import java.util.Objects;
import java.util.UUID;
import littleware.base.Maybe;
import littleware.base.Option;

import static littleware.base.validate.ValidatorUtil.check;

/**
 * Little pojo with the core information about a session
 * returned to a client or stored in a cookie.
 */
public class SessionCreds {
  public final UUID sessionId;
  
  /**
   * Constructor intended for internal use only ... obtain
   * the current creds via SessionInfo.getCredentials
   */
  public SessionCreds( 
          UUID sessionId
          ) {
    this.sessionId = sessionId;
    check( null != sessionId, "illegal null sessionId" );
  }
  
  /**
   * Returns Option(this) if this is an instance of LoginCreds -
   * just a shortcut for Maybe.something( LoginCreds.class.cast( this ) ) ...
   */
  public Option<LoginCreds> getLoginCreds() { return Maybe.empty(); }
  
  @Override
  public boolean equals( Object other ) {
    if( (other != null) && (other instanceof SessionCreds) ) {
      final SessionCreds b = (SessionCreds) other;
      return b.sessionId.equals( sessionId );
    }
    return false;
  }  

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 37 * hash + Objects.hashCode(this.sessionId);
    return hash;
  }
}
