/*
 * Copyright 2011 http://code.google.com/p/littleware
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.web.servlet.login;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import littleware.base.Options;
import littleware.base.Option;
import littleware.web.servlet.LittleServlet;
import littleware.web.servlet.helper.JsonResponse;
import littleware.web.servlet.helper.ResponseHelper;
import littleware.web.servlet.login.controller.*;
import littleware.web.servlet.login.model.*;


/**
 * Exports servlet methods: login, logout, sessionInfo ... 
 * works in conjunction with LoginFilter
 */
public class LoginServlet implements LittleServlet {

  private static final Logger log = Logger.getLogger(LoginServlet.class.getName());
  private final SessionMgr sessionMgr;
  private final SessionInfo initSession;
  private final Gson gsonTool;
  private final Provider<JsonResponse.Builder> responseFactory;
  private final ResponseHelper helper;

  @Inject
  public LoginServlet(
          SessionMgr sessionMgr,
          SessionInfo initSession, 
          Gson gsonTool,
          Provider<JsonResponse.Builder> responseFactory,
          ResponseHelper helper
          ) {
    this.sessionMgr = sessionMgr;
    this.initSession = initSession;
    this.gsonTool = gsonTool;
    this.responseFactory = responseFactory;
    this.helper = helper;
  }

  /**
   * Clear out cookies and whatever that shouldn't be left floating around after
   * a logout.
   *
   * @param request
   * @throws LoginException
   */
  public void zapSessionAfterLogout(HttpServletRequest request) {
    final Option<HttpSession> optSession = Options.some(request.getSession(false));
    if (optSession.nonEmpty()) {
      optSession.get().invalidate();
    }

    // destroy littleware session cookie ...
  }

  @Override
  public void doGetOrPostOrPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, java.io.IOException {
    final String action = Options.some( request.getParameter("action") ).getOr( "status" ).trim().toLowerCase();
    final JsonResponse.Builder respBuilder = responseFactory.get();
    
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

        final SessionInfo newSession = sessionMgr.login( initSession, user, password);
        activeSession = newSession;
      } catch ( LoginException ex) {
        log.log(Level.WARNING, "Login failed", ex);
        helper.write(response, respBuilder.status.set( HttpServletResponse.SC_FORBIDDEN ).build() );
        return;  // failed login is done
      }
    } else if (action.equalsIgnoreCase("logout")) {
        if ( sessionMgr.logout( initSession ) ) {
          zapSessionAfterLogout(request);
        }
        helper.write( response, respBuilder.build() );
        return;  // logout is done
    }
    
    //
    // finally - assemble a response with information about the active session,
    // and update the session cookie too ...
    //
    final com.google.gson.JsonObject js = sessionMgr.addSessionCookie( request, response, activeSession );
    final JsonResponse jsResponse = responseFactory.get().content.set(js).build();
    helper.write( response, jsResponse );
  }

}
