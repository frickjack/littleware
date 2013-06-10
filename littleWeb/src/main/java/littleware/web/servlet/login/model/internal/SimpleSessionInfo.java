/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.web.servlet.login.model.internal;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.UUID;
import littleware.base.Options;
import littleware.base.Option;
import littleware.bootstrap.SessionBootstrap;
import littleware.security.LittleUser;
import littleware.security.auth.LittleSession;
import littleware.security.auth.client.KeyChain;
import littleware.web.beans.GuiceBean;
import littleware.web.servlet.login.model.*;
import org.joda.time.DateTime;

/**
 * Little container for some session variables. The userFactory and
 * sessionFactory only work in authenticated sessions ...
 */
public class SimpleSessionInfo implements SessionInfo {

  private final Provider<LittleSession> sessionFactory;
  private final KeyChain keyChain;
  private final Provider<LittleUser> userFactory;
  private final UUID id;
  private final GuiceBean gBean;

  
  @Inject()
  public SimpleSessionInfo(
          KeyChain keyChain,
          Provider<LittleUser> userFactory,
          Provider<LittleSession> sessionFactory,
          GuiceBean gBean,
          SessionBootstrap sboot ) {
    this.keyChain = keyChain;
    this.userFactory = userFactory;
    this.sessionFactory = sessionFactory;
    this.id = sboot.getSessionId();
    this.gBean = gBean;
  }

  @Override
  public UUID getId() {
    return id;
  }

  /**
   * Get the guice bean associated with this session
   */
  @Override
  public GuiceBean getGBean() { return gBean; }
  
  /**
   * Return the currently logged in user if any
   */
  @Override
  public Option<LittleUser> getActiveUser() {
    if (keyChain.getDefaultSessionId().isSet()) {
      return Options.some(userFactory.get());
    } 
    return Options.empty();
  }


  @Override
  public SessionCreds getCredentials() {
    if (keyChain.getDefaultSessionId().isSet()) {
      final LittleUser user = getActiveUser().get();
      final LittleSession session = sessionFactory.get();
      final DateTime endDate = session.getEndDate() == null ? 
              DateTime.now().plusHours(24) : new DateTime( session.getEndDate().getTime() );
      return new LoginCreds( getId(), session.getId().toString(), endDate );
    } 
    
    return new SessionCreds( getId() );
  }

  
}
