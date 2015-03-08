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
import littleware.base.Options;
import littleware.base.Option;
import static littleware.base.validate.ValidatorUtil.check;
import org.joda.time.DateTime;

/**
 * Specialization of SessionCreds that includes
 * the authentication key necessary to
 * authenticate a session, and the expiration date
 * for the key.  The client should keep this data secret.
 */
public class LoginCreds extends SessionCreds {
  public final String authToken;
  public final DateTime expiration;
  
  public LoginCreds( UUID sessionId, String authToken, DateTime expiration ){
    super( sessionId );
    this.authToken = authToken;
    this.expiration = expiration;
    check( 
            null != sessionId && null != authToken && null != expiration, 
            "all properties given non-null values"
            );
  }
  
  private final Option<LoginCreds> me = Options.some( this );
  
  @Override
  public Option<LoginCreds> getLoginCreds() { return me; }
  
  @Override
  public boolean equals( Object other ) {
    if ( super.equals(other) && (other instanceof LoginCreds) ) {
      final LoginCreds b = (LoginCreds) other;
      
      return b.authToken.equals( authToken ) &&
              Math.abs( b.expiration.getMillis() - expiration.getMillis() ) < 10000;
    }
    return false;
  }
  
  @Override
  public String toString() {
      return "LoginCreds(" + sessionId + "," + authToken + "," + this.expiration + ")";
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 17 * hash + Objects.hashCode(this.sessionId);
    hash = 17 * hash + Objects.hashCode(this.authToken);
    hash = 17 * hash + Objects.hashCode(this.expiration);
    return hash;
  }
}
