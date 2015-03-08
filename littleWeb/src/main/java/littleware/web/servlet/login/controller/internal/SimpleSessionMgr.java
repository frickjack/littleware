/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.web.servlet.login.controller.internal;

import littleware.web.servlet.login.controller.*;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import littleware.base.Option;
import littleware.base.Options;
import littleware.base.UUIDFactory;
import littleware.base.login.LoginCallbackHandler;
import littleware.bootstrap.AppBootstrap;
import static littleware.web.servlet.login.LoginFilter.littleCookie;
import littleware.web.servlet.login.model.*;
import org.joda.time.DateTime;

/**
 * Application scope singleton maintains cache of active sessions, and manages
 * creation of new sessions based on cookies or whatever. When a new request
 * comes in the following flow runs: <ul> <li> parse cookie <li> lookup session
 * <li> login session if necessary <li> save session to cache </ul>
 */
public class SimpleSessionMgr implements SessionMgr {
  private final static Logger log = Logger.getLogger( SimpleSessionMgr.class.getCanonicalName() );
  private final AppBootstrap boot;
  private final LoadingCache<UUID, SessionInfo> sessionCache =
          CacheBuilder.newBuilder().maximumSize(1000).expireAfterWrite(24, TimeUnit.HOURS).build(
          new CacheLoader<UUID, SessionInfo>() {
            @Override
            public SessionInfo load(UUID key) {
              return boot.newSessionBuilder().sessionId(key).build().startSession(SessionInfo.class);
            }
          });
  private final com.google.gson.Gson gsonTool;

  @Inject()
  public SimpleSessionMgr(AppBootstrap boot, com.google.gson.Gson gsonTool ) {
    this.boot = boot;
    this.gsonTool = gsonTool;
  }

  @Override
  public SessionInfo loadSession(UUID id) {
    return sessionCache.getUnchecked(id);
  }

  @Override
  public SessionInfo login(SessionInfo session, String user, String authKey) throws LoginException {
    if (session.getActiveUser().isEmpty()) { // nobody logged in
      // need login configuration tied to session's underlying Guice session scope ...
      final Configuration loginConfig = session.getGBean().getInstance(Configuration.class);
      final LoginContext ctx = new LoginContext("littleware", new Subject(), new LoginCallbackHandler(user, authKey),
              loginConfig);
      ctx.login();
      // side effect of successful littleware-client JAAS authentication updates session creds
      return session;
    } else if (session.getActiveUser().get().getName().equals(user)) { // already logged in as user
      return session;
    } else { // somebody else logged in - logout of this session, and setup a new one ...
      final SessionInfo newSession = loadSession(UUID.randomUUID());
      return login(newSession, user, authKey);
    }
  }

  @Override
  public JsonObject toJson(SessionCreds creds) {
    final JsonObject js = new JsonObject();
    js.addProperty("id", creds.sessionId.toString());
    if (creds instanceof LoginCreds) {
      final LoginCreds lcreds = (LoginCreds) creds;
      js.addProperty("authToken", lcreds.authToken);
      js.addProperty("authExpires", lcreds.expiration.toString());
      
    }
    return js;
  }

  @Override
  public SessionCreds fromJson(final JsonObject js) {
    final UUID id = UUID.fromString(js.get("id").getAsString());
    if (js.has("authToken")) {
      return new LoginCreds(
              id, js.get("authToken").getAsString(),
              DateTime.parse(js.get("authExpires").getAsString())
              );
    }
    return new SessionCreds(id);
  }

  @Override
  public SessionInfo loadSession(SessionCreds creds) throws LoginException {
    final SessionInfo session = loadSession(creds.sessionId);
    if (creds.getLoginCreds().isEmpty()) {
      return session;
    }
    
    // support auto-authentication if login creds included
    final LoginCreds lcreds = creds.getLoginCreds().get();
    if (lcreds.equals(session.getCredentials())) {
      // cached session authenticated with same user
      return session;
    }
    
    //
    // little hack here - the authToken assigned by
    // the LoginServlet is a littleware sessionId UUID,
    // and the client login-module recognizes UUID user-names
    // as littleware sessionId (ignoring the password, since
    // the sessionId is a sufficient secret to access littleware API's).
    // 
    return login(session, lcreds.authToken, lcreds.authToken);
  }

  @Override
  public boolean logout(SessionInfo info) {
    if (info.getActiveUser().isSet()) {
      try {
        // need login configuration tied to session's underlying Guice session scope ...
        final Configuration loginConfig = info.getGBean().getInstance(Configuration.class);
        final LoginContext ctx = new LoginContext("littleware", new Subject(), new LoginCallbackHandler("", ""),
                loginConfig);
        ctx.logout();
        return true;
      } catch (LoginException ex) {
        log.log(Level.WARNING, "Caught exception on logout", ex);
      }
    }
    return false;
  }
  
  
  @Override
   public JsonObject addSessionCookie( 
          HttpServletRequest req, 
          HttpServletResponse resp, 
          SessionInfo activeSession 
          ) {
    final JsonObject js = toJson( activeSession.getCredentials() );
    // add user property for javascript client to latch onto
    final String user;
    if ( activeSession.getActiveUser().isSet() ) {
        user = activeSession.getActiveUser().get().getName();
    } else {
        user = "unknown";
    }
    js.addProperty( "user", user );
    
    final String json = gsonTool.toJson( js );
    final Cookie cookie = new Cookie(littleCookieName, json );
    cookie.setMaxAge(60 * 60 * 24);  // 1 day
    cookie.setPath(req.getContextPath().isEmpty() ? "/" : req.getContextPath());
    resp.addCookie(cookie);    
    return js;
  }

   
    
   @Override
    public AuthKey authorizeRequest(
            ImmutableList<Cookie> cookies,
            Option<String> optLittleSessionIdHeader
    ) {
        final Option<SessionCreds> optCookieCreds;
        final Option<SessionCreds> optHeaderCreds;
        
        // flag - should the filter add an littleCookie to the response ?
        boolean addCookie = false;
        
        // check cookies then headers for credentials
        {
            JsonObject json = null;
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(littleCookie)) {
                    json = gsonTool.fromJson(cookie.getValue(), JsonElement.class).getAsJsonObject();
                    break;
                }
            }
            if ( null != json ) {
                optCookieCreds = Options.some( fromJson(json) );
            } else optCookieCreds = Options.empty();
        }
            
        if ( optLittleSessionIdHeader.isSet() ) {
            final UUID headerKey = UUIDFactory.parseUUID( optLittleSessionIdHeader.get() );
            // note - just assign bogus end-date here - 
            //    littleware session's expiration date is accessed upon successful
            //    authentication.  Ugh - too much magic.
            //
            final SessionCreds headerCreds = new LoginCreds(UUID.randomUUID(), UUIDFactory.makeCleanString(headerKey), DateTime.now().plusDays(1));

            addCookie = true;  // header auth info supercedes cookie creds
            if ( optCookieCreds.isSet() && optCookieCreds.get().getLoginCreds().isSet() ) {
                final UUID cookieKey = UUIDFactory.parseUUID( optCookieCreds.get().getLoginCreds().get().authToken );
                if ( headerKey.equals( cookieKey ) ) {
                    // then continue using the previously established session-id in the cookie
                    optHeaderCreds = optCookieCreds;
                    addCookie = false;
                } else optHeaderCreds = Options.some( headerCreds );
            } else optHeaderCreds = Options.some( headerCreds );
        } else optHeaderCreds = Options.empty();
        
        // which creds to use - cookie or header ?  
        final SessionCreds selectedCreds;
        
        if ( optHeaderCreds.isSet() ) {
            selectedCreds = optHeaderCreds.get();
        } else if ( optCookieCreds.isSet() ) {
            selectedCreds = optCookieCreds.get();
        } else {
            // setup an unauthenticated session
            selectedCreds = new SessionCreds( UUID.randomUUID() );
            addCookie = true;
        }
        
        SessionInfo sinfo;
        try {
            sinfo = loadSession( selectedCreds );
        } catch ( LoginException ex ) {
            log.log( Level.WARNING, "Failed to authenticate session with creds: " + selectedCreds );
            // setup an unauthenticated session
            addCookie = true;
            sinfo = loadSession( UUID.randomUUID() );
        }
        
        final boolean authenticated = sinfo.getCredentials().getLoginCreds().isSet()
                        && sinfo.getCredentials().getLoginCreds().get().expiration.isAfter(DateTime.now());
        return new AuthKey( authenticated, sinfo, addCookie );
        
    }

}
