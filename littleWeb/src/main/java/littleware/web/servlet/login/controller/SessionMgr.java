/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.web.servlet.login.controller;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import java.util.UUID;
import javax.security.auth.login.LoginException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import littleware.web.servlet.login.model.*;


/**
 * Application scope singleton maintains cache of active sessions,
 * and manages creation of new sessions based on cookies or whatever.
 * When a new request comes in the following flow runs:
 * <ul>
 * <li> parse cookie
 * <li> lookup session
 * <li> login session if necessary
 * <li> save session to cache
 * </ul>
 */
public interface SessionMgr {
  /**
   * Load the session with the given id from cache, or create a new session
   */
  SessionInfo loadSession( UUID id );
  
  /**
   * Basically same as loadSession; and login() if SessionCreds
   * is an LoginCreds instance ...
   */
  SessionInfo loadSession( SessionCreds creds ) throws LoginException;

  /**
   * Use the given credentials to authenticate the given session.
   * return a new session, otherwise just return this if user is already logged
   * id.  It's the responsibility of the caller to reset the
   * cookie or whatever on the HTTP request with the new data from
   * the returned SessionInfo.
   * 
   * @param user should match name in server's user database
   * @param authToken password or other secret token shared between client and server
   */
  SessionInfo login( SessionInfo info, String user, String authToken ) throws LoginException;

  /**
   * Return true if session is logged out, false if session wasn't logged in
   * in the first place
   * 
   * @param info
   */
  boolean logout( SessionInfo info );
  
  /**
   * Simple json serialize
   */
  JsonObject toJson( SessionCreds creds );

  /**
   * Simple json deserialize
   */  
  SessionCreds fromJson( final JsonObject js );
  
  public static final String littleCookieName = "littleCookie";  
  
  /**
   * Little utility - adds the credentials for the given session
   * to the response as a json object via toJson( activeSession.getCredentials() ) ...
   * 
   * @param req to derive cookie path from
   * @param resp to add cookie to
   * @return the json object set in the cookie
   */
   JsonObject addSessionCookie( HttpServletRequest req, HttpServletResponse resp, SessionInfo activeSession );  

   
    /**
     * Result container for authorizeRequest() below
     */
    public static class AuthKey {
        public final boolean authenticated;
        public final SessionInfo info;
        public final boolean addCookie;
        
        public AuthKey( boolean authenticated, SessionInfo info, boolean addCookie ) {
            this.authenticated = authenticated;
            this.info = info;
            this.addCookie = addCookie;
        }
    }

    /**
     * Authorize a request to access the resource this filter defends
     * based on the request's littleCookie or little-sessionId header.
     * Note that the request header actively assigned by the client takes precedence
     * over a cookie value - which may be stale.
     * 
     * @param cookies attached to the HTTP request - scanned for littleCookie
     * @param optLittleSessionIdHeader - "littleware-sessionId" header attached to request if any
     * @return AuthKey that either authorizes or denies access
     */
    public AuthKey authorizeRequest(
            ImmutableList<Cookie> cookies,
            Option<String> optLittleSessionIdHeader
    );

}
