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
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
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
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import littleware.apps.client.ClientBootstrap;
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
public class LoginHandler extends HttpServlet implements HttpSessionListener, Filter {

    private static final Logger log = Logger.getLogger(LoginHandler.class.getName());
    Maybe<GuiceBean> maybeGuest = Maybe.empty();

    private void sessionCreated(HttpSession session) {
        final GuiceBean bean = (GuiceBean) session.getAttribute(WebBootstrap.littleGuice);
        if (null == bean) {
            setupGuest();
            session.setAttribute(WebBootstrap.littleGuice, maybeGuest.get());
        }
    }

    /**
     * Inject an unauthenticated GuiceBean into the session
     */
    @Override
    public void sessionCreated(HttpSessionEvent event) {
        sessionCreated(event.getSession());
    }

    private void sessionDestroyed(HttpSession session) {
        final LittleBootstrap boot = (LittleBootstrap) session.getAttribute(WebBootstrap.littleBoot);
        if (null != boot) {
            boot.shutdown();
            session.removeAttribute(WebBootstrap.littleBoot);
        }
        session.removeAttribute(WebBootstrap.littleGuice);
    }

    /**
     * Shutdown the littleware OSGi environment associated
     * with this session (if any)
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        final HttpSession session = event.getSession();
        sessionDestroyed(session);
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
     * Cache a littleware session for the guest user.
     */
    private void setupGuest() {
        if (maybeGuest.isEmpty()) {
            try {
                final Properties properties = PropertiesLoader.get().loadProperties();
                final String guest = properties.getProperty("web.guest");
                final String guestPassword = properties.getProperty("web.guest.password");
                if ((guest != null) && (guestPassword != null)) {
                    log.log( Level.INFO, "Logging in guest user: " + guest );
                    final SessionManager manager = SessionUtil.get().getSessionManager();
                    /// TODO - lookup guest password from littleware.properties or whatever
                    final SessionHelper helper = manager.login(guest, guestPassword, "web login");
                    new ClientBootstrap(new ClientServiceGuice(helper)) {

                        @Override
                        public void bootstrap() {
                            maybeGuest = Maybe.something((GuiceBean) new GuiceBean(super.bootstrapInternal()) {

                                @Override
                                public boolean isLoggedIn() {
                                    return false;
                                }
                            });
                        }
                    }.bootstrap();
                }
            } catch (Exception ex) {
                log.log(Level.WARNING, "Failed to setup guest-user web-session - just using empty GuiceBean instead", ex);
            } finally {
                if (maybeGuest.isEmpty()) {
                    maybeGuest = Maybe.something(new GuiceBean());
                }
            }
            //config.getServletContext().setAttribute( WebBootstrap.littleGuice, maybeGuest.get() );
        }
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
                final SessionManager manager = SessionUtil.get().getSessionManager();
                final SessionHelper helper = manager.login(request.getParameter("user"), request.getParameter("password"), "web login");
                // login ok!
                String forwardURL = request.getParameter("okURL");
                if (null == forwardURL) {
                    forwardURL = this.getLoginOkURL();
                }
                final HttpSession session = request.getSession();
                {
                    final GuiceBean bean = (GuiceBean) session.getAttribute(WebBootstrap.littleGuice);
                    if (null != bean) {
                        // already logged in as another user - clear out that session, and create a new one
                        logout(request);
                        // create a new session
                        //session = request.getSession( true );
                        //throw new IllegalStateException("Session already logged in");
                    }
                }
                final WebBootstrap boot = new WebBootstrap(session, helper);
                boot.bootstrap();
                try {
                    //response.getWriter().println( "<html><head></head><body>OK</body></html>" );
                    response.getWriter().println("<html><head><meta http-equiv=\"refresh\" content=\"2;url="
                            + request.getContextPath() + forwardURL
                            + "\"/></head><body>OK ... <a href=\""
                            + request.getContextPath() + forwardURL + "\">redirecting ...</a>"
                            + "</body></html>");
                    //response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
                    //response.setHeader("Location", forwardURL );
                    //getServletConfig().getServletContext().getRequestDispatcher(forwardURL).include(request, response);
                    //response.sendRedirect( request.getContextPath() + forwardURL);
                    return;
                } catch (Exception ex2) {
                    log.log(Level.WARNING, "Failed to forward to " + forwardURL, ex2);
                    throw new ServletException("Bad URL: " + forwardURL, ex2);
                }
            } catch (ServletException ex) {
                throw ex;
            } catch (Exception ex) {
                log.log(Level.WARNING, "Login failed", ex);
                //throw new ServletException( "Login failed", ex );

                String forwardURL = request.getParameter("failedURL");
                if (null == forwardURL) {
                    forwardURL = this.getLoginFailedURL();
                }
                request.setAttribute("exception", ex);
                try {
                    response.getWriter().println("<html><head><meta http-equiv=\"refresh\" content=\"2;url="
                            + request.getContextPath() + forwardURL
                            + "\"/></head><body>OK ... <a href=\""
                            + request.getContextPath() + forwardURL + "\">redirecting ...</a>"
                            + "</body></html>");
                    //response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
                    //response.setHeader("Location", "http://yahoo.com");
                    //getServletConfig().getServletContext().getRequestDispatcher(forwardURL).include(request, response);
                    //response.sendRedirect( request.getContextPath() + forwardURL);
                    return;
                } catch (Exception ex2) {
                    log.log(Level.WARNING, "Failed to forward to " + forwardURL, ex2);
                    throw new ServletException("Bad URL: " + forwardURL, ex2);
                }
            }
        } else if (action.equalsIgnoreCase("logout")) {
            try {
                logout(request);
                String forwardURL = request.getParameter("logoutURL");
                if (null == forwardURL) {
                    forwardURL = this.getLogoutURL();
                }
                try {
                    response.getWriter().println("<html><head><meta http-equiv=\"refresh\" content=\"2;url="
                            + request.getContextPath() + forwardURL
                            + "\"/></head><body>OK ... <a href=\""
                            + request.getContextPath() + forwardURL + "\">redirecting ...</a>"
                            + "</body></html>");

                    //response.getWriter().println( "<html><head></head><body>OK</body></html>" );
                    //response.setStatus( HttpServletResponse.SC_MOVED_TEMPORARILY );
                    //response.setHeader( "Location", "http://yahoo.com" );
                    //getServletConfig().getServletContext().getRequestDispatcher(forwardURL).forward(request, response);
                    //response.sendRedirect( request.getContextPath() + forwardURL);
                    return;
                } catch (Exception ex2) {
                    log.log(Level.WARNING, "Failed to forward to " + request.getContextPath() + "/" + forwardURL, ex2);
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
     * Shutdown the littleware OSGi environment associated
     * with this session (if any), and invalidate the session.
     * @param session
     */
    @VisibleForTesting
    public void logout(HttpServletRequest request) {
        final HttpSession session = request.getSession();
        sessionDestroyed(session);

        // Just unbind all the data in the session!
        for (Enumeration<String> scan = session.getAttributeNames();
                scan.hasMoreElements();) {
            session.removeAttribute(scan.nextElement());
        }
        //session.invalidate();
        sessionCreated(session);
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
            sessionCreated(((HttpServletRequest) request).getSession());
        } catch ( IllegalStateException ex ) {
            log.log( Level.INFO, "Ignoring webapp state exception setting up session - some weird glassfish race condition", ex );
        }
        chain.doFilter(request, response);
    }
}
