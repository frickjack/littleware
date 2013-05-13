/*
 * Copyright 2011 http://code.google.com/p/littleware
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.web.servlet.login;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import littleware.base.Maybe;
import littleware.base.Option;
import littleware.security.auth.LittleSession;
import littleware.web.servlet.LittleServlet;
import littleware.web.servlet.helper.JsonResponse;

/**
 * Exports servlet methods: login, logout, sessionInfo ... 
 * works in conjunction with LoginFilter
 */
public class LoginServlet implements LittleServlet {

  public static final String littleCookieName = "littleCookie";
  private static final Logger log = Logger.getLogger(LoginServlet.class.getName());
  private final SessionInfo initSession;
  private final Gson gsonTool;
  private final Provider<JsonResponse.Builder> responseFactory;

  @Inject
  public LoginServlet(
          SessionInfo initSession, 
          Gson gsonTool,
          Provider<JsonResponse.Builder> responseFactory
          ) {
    this.initSession = initSession;
    this.gsonTool = gsonTool;
    this.responseFactory = responseFactory;
  }

  /*  
   private void eraseCookie(HttpServletRequest req, HttpServletResponse resp) {
   Cookie[] cookies = req.getCookies();
   if (cookies != null)
   for (int i = 0; i < cookies.length; i++) {
   cookies[i].setValue("");
   cookies[i].setPath("/");
   cookies[i].setMaxAge(0);
   resp.addCookie(cookies[i]);
   }
   }
   */
  /**
   * Clear out cookies and whatever that shouldn't be left floating around after
   * a logout.
   *
   * @param request
   * @throws LoginException
   */
  public void zapSessionAfterLogout(HttpServletRequest request) {
    final Option<HttpSession> optSession = Maybe.something(request.getSession(false));
    if (optSession.nonEmpty()) {
      optSession.get().invalidate();
    }

    // destroy littleware session cookie ...
  }

  @Override
  public void doGetOrPostOrPut(HttpServletRequest request, HttpServletResponse response) throws ServletException {
    final String action;
    {
      final String temp = request.getParameter("action");

      if (null == temp) {
        throw new IllegalArgumentException("action parameter not specified");
      } else {
        action = temp.trim();
      }
    }

    final JsonResponse.Builder respBuilder = responseFactory.get();
    assert( false ); // TODO: need to integrate reponse builder ...
    
    SessionInfo activeSession = initSession;

    if (action.equalsIgnoreCase("login")) {
      // take a user-name and a password, authenticate, assign a
      //  session-id to the frickjack
      try {
        final String user = request.getParameter("user");
        final String password = request.getParameter("password");
        if (null == user || null == password) {
          throw new ServletException("Must specify user and password parameters");
        }

        final SessionInfo newSession = initSession.login(user, password);
        activeSession = newSession;
      } catch (Exception ex) {
        log.log(Level.WARNING, "Login failed", ex);
        throw new ServletException("Login failed", ex);
      }
    } else if (action.equalsIgnoreCase("logout")) {
      try {
        if (initSession.logout()) {
          zapSessionAfterLogout(request);
        }
      } catch (LoginException ex) {
        log.log(Level.WARNING, "Ignoring failed logout", ex);
        zapSessionAfterLogout(request);
      }
    }
    
    //
    // finally - assemble a response with information about the active session,
    // and update the session cookie too ...
    //
    final String json = gsonTool.toJson( activeSession.toJson() );
    final Cookie cookie = new Cookie(littleCookieName, json );
    cookie.setMaxAge(60 * 60 * 24);  // 1 day
    cookie.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
    response.addCookie(cookie);    
  }

}
