/*
 * Copyright 2011 http://code.google.com/p/littleware
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.web.servlet;

import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import littleware.web.beans.GuiceBean;

/**
 * Combination servlet and HttpSessionListener and RequestFilter.
 * When a session is created injects a SessionBean
 * with an empty SessionHelper or a "guest" user session helper
 * depending on configuration.
 * If the servlet receives a request, then it looks
 * for:
 *        username, password, sessionId, urlSuccess, urlFailure
 * parameters.  The servlet attempts to authenticate
 * a new session via sessionId, username, and password as
 * appropriate.  If login fails, then bounces the user
 * to urlFailure.  If login succeeds, then if the HttpSession
 * isNew is false, then create a new session.
 * Once a new session is established, then
 * configure a new SessionBean and bootstrap environment
 * for the session.
 * Finally, at session shutdown time do any littleware cleanup
 * necessary.
 */
public class LoginHandler extends HttpServlet implements Filter {

    private static final Logger log = Logger.getLogger(LoginHandler.class.getName());


    /**
     * Allow init-time override of default "loginOkURL"
     * and "loginFailedURL" properties
     */
    @Override
    public void init() {
        final ServletConfig config = getServletConfig();
    }

    public void login( String username, String password ) {
      throw new UnsupportedOperationException( "not yet implemented" );
    }
    
    @VisibleForTesting
    public void logout(HttpServletRequest request) {
        final HttpSession session = request.getSession();
        session.invalidate();
        // destroy session ... bla
    }
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        doCommon(request, response);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        doCommon(request, response);
    }

    private void doCommon(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        final String action;
        {
          String temp = request.getParameter("action");

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
                //final ClientBootstrap boot = null; //ClientBootstrap.clientProvider.get().profile(AppBootstrap.AppProfile.WebApp).build().login(request.getParameter("user"), request.getParameter("password"));
                // login ok!
                final HttpSession session = request.getSession();
                if (null != (GuiceBean) session.getAttribute("guiceBean")) {
                    // already logged in as another user - clear out that session, and create a new one
                    logout(request);
                    // create a new session
                    //session = request.getSession( true );
                    //throw new IllegalStateException("Session already logged in");
                }
            } catch (Exception ex) {
                log.log(Level.WARNING, "Login failed", ex);
                throw new ServletException( "Login failed", ex );
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
}
