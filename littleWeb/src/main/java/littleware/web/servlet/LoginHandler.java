/*
 * Copyright 2011 http://code.google.com/p/littleware
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.web.servlet;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import littleware.base.login.LoginCallbackHandler;
import littleware.bootstrap.AppBootstrap;
import littleware.security.LittleUser;
import littleware.security.auth.LittleSession;
import littleware.security.auth.client.ClientLoginModule;
import littleware.web.beans.GuiceBean;

/**
 * Combination servlet and HttpSessionListener and RequestFilter. When a session
 * is created injects a SessionBean with an empty SessionHelper or a "guest"
 * user session helper depending on configuration. If the servlet receives a
 * request, then it looks for: username, password, sessionId, urlSuccess,
 * urlFailure parameters. The servlet attempts to authenticate a new session via
 * sessionId, username, and password as appropriate. If login fails, then
 * bounces the user to urlFailure. If login succeeds, then if the HttpSession
 * isNew is false, then create a new session. Once a new session is established,
 * then configure a new SessionBean and bootstrap environment for the session.
 * Finally, at session shutdown time do any littleware cleanup necessary.
 */
public class LoginHandler implements LittleServlet, Filter {

  private static final Logger log = Logger.getLogger(LoginHandler.class.getName());
  private final AppBootstrap boot;


  public LoginHandler( AppBootstrap boot ) {
    this.boot = boot;
  }



  
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

  @VisibleForTesting
  public void logout(HttpServletRequest request) {
    final HttpSession session = request.getSession(false);
    if (null != session) {
      session.invalidate();
    }
    // destroy littleware session cookie ...
  }
  
  /**
   * Little container for some session variables.
   * The userFactory and sessionFactory only work in authenticated sessions ...
   */
  public static class SessionInfo {
    public final Provider<LittleSession> sessionFactory;
    public final Provider<LittleUser> userFactory;
    public final Configuration loginConfig;
    
    @Inject()
    public SessionInfo( Configuration loginConfig,
            Provider<LittleUser> userFactory,
            Provider<LittleSession> sessionFactory
            ) {
      this.loginConfig = loginConfig;
      this.userFactory = userFactory;
      this.sessionFactory = sessionFactory;
    }
  }


  public static String littleSessionCookie = "littleSession";
  
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
    if (action.equalsIgnoreCase("login")) {
      // take a user-name and a password, authenticate, assign a
      //  session-id to the frickjack
      try {
        // first - logout the current user if necessary
        logout( request );
        final String user = request.getParameter( "user" );
        final String password = request.getParameter( "password" );

        if( null == user || null == password ) {
          throw new ServletException( "Must specify user and password parameters" );  
        }
        
        // start a new littleware session - with its own child injector, etc.
        final SessionInfo info = boot.newSessionBuilder().build().startSession( SessionInfo.class );
        
        final LoginContext ctx = new LoginContext( "littleware", new Subject(), new LoginCallbackHandler( user, password ), 
                info.loginConfig
                );
        ctx.login();
        
        // login succeeded - set a cookie with some info about the
        //   newly created littleware session ...
        final Cookie cookie = new Cookie( littleSessionCookie, info.sessionFactory.get().getId().toString() );
        cookie.setPath( request.getContextPath().isEmpty() ? "/" : request.getContextPath() );
        response.addCookie( cookie );
      } catch (Exception ex) {
        log.log(Level.WARNING, "Login failed", ex);
        throw new ServletException("Login failed", ex);
      }
    } else if (action.equalsIgnoreCase("logout")) {
      logout(request);
    } else {
      throw new IllegalArgumentException("Unknown action: " + action);
    }
  }

  @Override
  public void init(FilterConfig fc) throws ServletException {
    // noop
  }

  /**
   * Just make sure the freakin' littleware session is initialized ...
   */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    try {
      // TODO - setup session authenticating with session-id from cookie
      //   or request parameter ...
      //sessionCreated(((HttpServletRequest) request).getSession());
    } catch (IllegalStateException ex) {
      log.log(Level.INFO, "Ignoring webapp state exception setting up session - some weird glassfish race condition", ex);
    }
    chain.doFilter(request, response);
  }

  public void destroy() {
  }
}
