/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.web.servlet;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Injector;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import littleware.apps.client.ClientBootstrap;
import littleware.base.BaseException;
import littleware.base.Maybe;
import littleware.base.PropertiesLoader;
import littleware.security.auth.ClientServiceGuice;
import littleware.security.auth.LittleBootstrap;
import littleware.security.auth.SessionHelper;
import littleware.security.auth.SessionManager;
import littleware.security.auth.SessionUtil;
import littleware.web.beans.GuiceBean;

/**
 * Combination servlet and HttpSessionListener.
 * When a session is created injects a SessionMean
 * with an empty SessionHelper or a "guest" session helper
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
public class LoginHandler extends HttpServlet implements HttpSessionListener {

    private static final Logger log = Logger.getLogger(LoginHandler.class.getName());

    Maybe<GuiceBean> maybeGuest = Maybe.empty();

    /**
     * Inject an unauthenticated GuiceBean into the session
     */
    @Override
    public void sessionCreated(HttpSessionEvent event) {
        if (!maybeGuest.isSet()) {
            /**
             * Cache a littleware session for the guest user.
             */
            try {
                final Properties properties = PropertiesLoader.get().loadProperties();
                final String guest = properties.getProperty("web.guest");
                final String guestPassword = properties.getProperty("web.guest.password");
                if ((guest != null) && (guestPassword != null)) {
                    final SessionManager manager = SessionUtil.get().getSessionManager();
                    /// TODO - lookup guest password from littleware.properties or whatever
                    final SessionHelper helper = manager.login("guest", "guest", "web login");
                    new ClientBootstrap( new ClientServiceGuice( helper ) ) {
                        @Override
                        public void bootstrap() {
                            maybeGuest = Maybe.something( (GuiceBean)
                                    new GuiceBean( super.bootstrapInternal() ) {
                                @Override
                                public boolean isLoggedIn() { return false; }
                            } );
                        }
                    }.bootstrap();
                } 
            } catch (Exception ex) {
                log.log(Level.WARNING, "Failed to setup guest-user web-session - just using empty GuiceBean instead", ex );
            } finally {
                if ( maybeGuest.isEmpty() ) {
                    maybeGuest = Maybe.something( new GuiceBean() );
                }
            }
        }
        event.getSession().setAttribute(WebBootstrap.littleGuice, maybeGuest.get());
    }

    /**
     * Shutdown the littleware OSGi environment associated
     * with this session (if any)
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        final LittleBootstrap boot = (LittleBootstrap) event.getSession().getAttribute(WebBootstrap.littleBoot);
        if (null != boot) {
            boot.shutdown();
            event.getSession().removeAttribute(WebBootstrap.littleBoot);
        }
        event.getSession().removeAttribute(WebBootstrap.littleGuice);
    }
    String loginOkURL = "/login/welcome.jsp";
    String loginFailedURL = "/login/ugh.jsp";
    String logoutURL = "/login/goodbye.jsp";

    public String getLoginFailedURL() {
        return loginFailedURL;
    }

    public void setLoginFailedURL(String loginFailedURL) {
        this.loginFailedURL = loginFailedURL;
    }

    public String getLogoutURL() {
        return logoutURL;
    }

    public void setLogoutURL(String logoutURL) {
        this.logoutURL = logoutURL;
    }

    public String getLoginOkURL() {
        return loginOkURL;
    }

    public void setLoginOkURL(String loginOkURL) {
        this.loginOkURL = loginOkURL;
    }

    /**
     * Allow init-time override of default "loginOkURL"
     * and "loginFailedURL" properties
     */
    @Override
    public void init() {
        final ServletConfig config = getServletConfig();
        final String loginOkParam = config.getInitParameter("loginOkURL");
        if (null != loginOkParam) {
            loginOkURL = loginOkParam;
        }
        final String loginFailedParam = config.getInitParameter("loginFailedURL");
        if (null != loginFailedParam) {
            loginFailedURL = loginFailedParam;
        }
        final String logoutParam = config.getInitParameter("logoutURL");
        if (null != logoutParam) {
            logoutURL = logoutParam;
        }
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
        String action = request.getParameter("action");

        if (null == action) {
            throw new IllegalArgumentException("action parameter not specified");
        } else {
            action = action.trim();
        }
        if (action.equalsIgnoreCase("login")) {
            try {
                login(request, request.getParameter("user"), request.getParameter("password"));
                String forwardURL = request.getParameter("okURL");
                if (null == forwardURL) {
                    forwardURL = this.getLoginOkURL();
                }
                try {
                    getServletConfig().getServletContext().getRequestDispatcher(forwardURL).forward(request, response);
                } catch (IOException ex2) {
                    throw new ServletException("Bad URL: " + forwardURL, ex2);
                }
            } catch (ServletException ex) {
                throw ex;
            } catch (Exception ex) {
                String forwardURL = request.getParameter("failedURL");
                if (null == forwardURL) {
                    forwardURL = this.getLoginFailedURL();
                }
                request.setAttribute("exception", ex);
                try {
                    getServletConfig().getServletContext().getRequestDispatcher(forwardURL).forward(request, response);
                } catch (IOException ex2) {
                    throw new ServletException("Bad URL: " + forwardURL, ex2);
                }
            }
        } else if (action.equalsIgnoreCase("logout")) {
            try {
                logout(request.getSession());
                String forwardURL = request.getParameter("logoutURL");
                if (null == forwardURL) {
                    forwardURL = this.getLogoutURL();
                }
                try {
                    getServletConfig().getServletContext().getRequestDispatcher(forwardURL).forward(request, response);
                } catch (IOException ex2) {
                    throw new ServletException("Bad URL: " + forwardURL, ex2);
                }
            } catch (ServletException ex) {
                throw ex;
            }
        } else {
            throw new IllegalArgumentException("Unknown action: " + action);
        }

    }

    /**
     * Attempts to authenticate the user.
     * On success, sets up a new session with a
     * logged in GuiceBean.
     * Throws IllegalStateException if session already logged
     * in under a different user.
     * 
     * @param session
     * @param user
     * @param password
     * @throws BaseException
     * @throws GeneralSecurityException on authentication failure
     * @throws RemoteException
     */
    @VisibleForTesting
    public void login(HttpServletRequest request, String user,
            String password) throws BaseException, GeneralSecurityException, RemoteException, NotBoundException {
        HttpSession session = request.getSession( false );
        if ( null != session ) {
            final GuiceBean bean = (GuiceBean) session.getAttribute(WebBootstrap.littleGuice);
            if ( null != bean ) {
                // already logged in as another user - clear out that session, and create a new one
                logout( session );
                //throw new IllegalStateException("Session already logged in");
                session = request.getSession( true );
            }
        }
        final SessionManager manager = SessionUtil.get().getSessionManager();
        final SessionHelper helper = manager.login(user, password, "web login");
        final WebBootstrap boot = new WebBootstrap(session, helper);
        boot.bootstrap();
    }

    /**
     * Shutdown the littleware OSGi environment associated
     * with this session (if any), and invalidate the session.
     * @param session
     */
    @VisibleForTesting
    public void logout(HttpSession session) {
        sessionDestroyed(new HttpSessionEvent(session));
        session.invalidate();
    }
}
