/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.web.servlet.login;

import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.UUID;
import javax.security.auth.Subject;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import littleware.base.Maybe;
import littleware.base.Option;
import littleware.base.login.LoginCallbackHandler;
import littleware.bootstrap.AppBootstrap;
import littleware.bootstrap.SessionBootstrap;
import littleware.security.LittleUser;
import littleware.security.auth.LittleSession;
import littleware.security.auth.client.KeyChain;
import littleware.web.beans.GuiceBean;
import org.joda.time.DateTime;

/**
 * Little container for some session variables. The userFactory and
 * sessionFactory only work in authenticated sessions ...
 */
public class SessionInfo {

  private final Provider<LittleSession> sessionFactory;
  private final KeyChain keyChain;
  private final Provider<LittleUser> userFactory;
  private final AppBootstrap boot;
  private final Configuration loginConfig;
  private final UUID id;

  @Inject()
  public SessionInfo(
          AppBootstrap boot,
          Configuration loginConfig,
          KeyChain keyChain,
          Provider<LittleUser> userFactory,
          Provider<LittleSession> sessionFactory,
          GuiceBean gBean,
          SessionBootstrap sboot) {
    this.boot = boot;
    this.loginConfig = loginConfig;
    this.keyChain = keyChain;
    this.userFactory = userFactory;
    this.sessionFactory = sessionFactory;
    this.id = sboot.getSessionId();
  }

  public UUID getId() {
    return id;
  }

  /**
   * Return the currently logged in user if any
   */
  public Option<LittleUser> getActiveUser() {
    if ( getLoginSession().nonEmpty() ) {
      return Maybe.something(userFactory.get());
    } 
    return Maybe.empty();
  }

  /**
   * Return the currently active littleware login-session if any
   *
   * @return
   */
  public Option<LittleSession> getLoginSession() {
    if (keyChain.getDefaultSessionId().isSet()) {
      final LittleSession session = sessionFactory.get();
      if ( DateTime.now().isBefore( new DateTime( session.getEndDate() ) ) ) {
         return Maybe.something( session );
      }
    } 
    return Maybe.empty();
  }

  /**
   * Convenience method - assembles the LoginContext and logs out if
   * getActiveUser().isSet()
   *
   * @return true if logged out, false if logout not necessary for
   * unauthenticated session
   */
  public boolean logout() throws LoginException {
    if (keyChain.getDefaultSessionId().isSet()) {
      final LoginContext ctx = new LoginContext("littleware", new Subject(), new LoginCallbackHandler("ignore", "ignore"),
              loginConfig);
      ctx.logout();
      return true;
    }
    return false;
  }

  /**
   * Login the given user if user != getActiveUser to the active session, and
   * return a new session, otherwise just return this if user is already logged
   * id.
   */
  public SessionInfo login(String user, String password) throws LoginException {
    final Option<LittleUser> optCurrentUser = getActiveUser();

    if (optCurrentUser.isEmpty()) { // nobody logged in
      final LoginContext ctx = new LoginContext("littleware", new Subject(), new LoginCallbackHandler(user, password),
              loginConfig);
      ctx.login();
      return this;
    } else if (optCurrentUser.get().getName().equals(user)) { // already logged in as user
      return this;
    } else { // somebody else logged in - logout of this session, and setup a new one ...
      final SessionInfo newSession = boot.newSessionBuilder().build().startSession(SessionInfo.class);
      newSession.login(user, password);
      return newSession;
    }
  }

  public JsonObject toJson() {
    final JsonObject js = new JsonObject();
    js.addProperty("id", getId().toString());
    final Option<LittleUser> optActiveUser = getActiveUser();
    final Option<LittleSession> optLoginSession = getLoginSession();
    if (optActiveUser.nonEmpty()) {
      js.addProperty("user", getActiveUser().get().getName());
      final LittleSession loginSession = getLoginSession().get();
      js.addProperty("authKey", loginSession.getId().toString());
      js.addProperty("authExpires", new org.joda.time.DateTime(loginSession.getEndDate()).toString());
    }
    return js;
  }
}
