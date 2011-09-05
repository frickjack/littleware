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
import com.google.inject.Injector;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.UUID;
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
import littleware.asset.client.AssetManager;
import littleware.asset.client.AssetSearchManager;
import littleware.base.Maybe;
import littleware.base.Option;
import littleware.base.PropertiesLoader;
import littleware.bootstrap.AppBootstrap;
import littleware.bootstrap.LittleBootstrap;
import littleware.security.auth.LittleSession;
import littleware.web.beans.GuiceBean;
import org.joda.time.DateTime;

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

    /**
     * Internal handler manages sharing of a single littleware 'guest' session.
     * Public to allow non-AOP guice access to class info.
     */
    public static class GuestManager {

        /**
         * Specialization of GuiceBean where isLoggedIn == false
         */
        public static class GuestGuiceBean extends GuiceBean {

            @Inject
            public GuestGuiceBean(Injector injector) {
                super(injector);
            }

            @Override
            public boolean isLoggedIn() {
                return false;
            }
        }

        /**
         * Little pojo holds a bundle of session data
         */
        public static class SessionInfo {

            private final GuiceBean bean;
            private final UUID sessionId;
            private final AssetManager assetMgr;
            private final AssetSearchManager search;
            private final LittleBootstrap boot;

            @Inject
            public SessionInfo(GuestGuiceBean guestBean, LittleSession session, AssetManager assetMgr,
                    AssetSearchManager search, LittleBootstrap boot) {
                this.bean = guestBean;
                this.sessionId = session.getId();
                this.assetMgr = assetMgr;
                this.search = search;
                this.boot = boot;
            }

            public GuiceBean getBean() {
                return bean;
            }

            public UUID getSessionId() {
                return sessionId;
            }

            public LittleBootstrap getBootstrap() {
                return boot;
            }

            public AssetSearchManager getSearchManager() {
                return search;
            }

            public AssetManager getAssetManager() {
                return assetMgr;
            }
        }
        // ------------------------------------------------
        private Option<SessionInfo> maybeGuest = Maybe.empty();

        /**
         * Return a GuiceBean tied to a littleware session for the 'guest' user
         * for use in a new web session.
         *
         * @return a shared GuiceBean for use in multiple web sessions.
         */
        public synchronized GuiceBean loadGuestBean() {
            if (maybeGuest.isEmpty()) {
                try {
                    final Properties properties = PropertiesLoader.get().loadProperties();
                    final String guest = properties.getProperty("web.guest");
                    final String guestPassword = properties.getProperty("web.guest.password");
                    if ((guest != null) && (guestPassword != null)) {
                        log.log(Level.INFO, "Logging in guest user: {0}", guest);
                        final AppBootstrap boot = AppBootstrap.appProvider.get().build();
                        // Login as guest, bla bla bla
                        //ClientBootstrap.clientProvider.get().profile(AppBootstrap.AppProfile.WebApp).build().login(guest, guestPassword);
                        maybeGuest = Maybe.empty(); // Maybe.something(boot.startSession(SessionInfo.class));
                    } else {
                        log.log(Level.WARNING, "Guest user/password not set in littleware.properties - just using empty GuiceBean instead");
                        return new GuiceBean();
                    }
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Failed to setup guest-user web-session - just using empty GuiceBean instead", ex);
                    return new GuiceBean();
                }
                //config.getServletContext().setAttribute( WebBootstrap.littleGuice, maybeGuest.get() );
            }

            if (!maybeGuest.isSet()) {
                log.log(Level.WARNING, "Failed to setup guest-user web-session - just using empty GuiceBean instead");
                return new GuiceBean();
            }

            // prevent the littleware session from timing out under the web session
            try {
                final SessionInfo info = maybeGuest.get();
                final LittleSession session = info.getSearchManager().getAsset(info.getSessionId()).get().narrow();
                final DateTime sessionEnd = new DateTime(session.getEndDate());
                final DateTime now = new DateTime();
                final DateTime tomorrow = now.plusDays(1);

                if (now.isAfter(sessionEnd)) {
                    // session is expired - shut it down, and start a new one
                    final LittleBootstrap boot = info.getBootstrap();
                    maybeGuest = Maybe.empty();
                    try {
                        boot.shutdown();
                    } catch (Exception ex) {
                        log.log(Level.WARNING, "Shutdown of expired guest session caught exception", ex);
                    }
                    // recurse to setup a new session
                    return loadGuestBean();
                } else if (tomorrow.isAfter(sessionEnd)) {
                    // then extend session
                    info.getAssetManager().saveAsset(
                            session.copy().endDate(tomorrow.plusDays(1).toDate()).build(),
                            "extending web guest session");
                } // else - session is ok as is
            } catch (Exception ex) {
                log.log(Level.WARNING, "Failed to extend guest littleware session", ex);
            }

            return maybeGuest.get().getBean();
        }
    }
    // ----------------------------------------------------
    private GuestManager guestManager = new GuestManager();

    private void sessionCreated(HttpSession session) {
        final GuiceBean bean = (GuiceBean) session.getAttribute(WebBootstrap.littleGuice);

        if (null == bean) {
            session.setAttribute(WebBootstrap.littleGuice, guestManager.loadGuestBean());
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
            // Note: GuestManager does not register 'guest' session bootstrap handler with the web session
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
    private String loginOkURL = "/login/welcome.jsp";
    private String loginFailedURL = "/login/ugh.jsp";
    private String logoutURL = "/login/goodbye.jsp";

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
        throw new UnsupportedOperationException( "This thing is busted - needs 2b ported to littleware 2.5 session infrastructure" );
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
                //final ClientBootstrap boot = null; //ClientBootstrap.clientProvider.get().profile(AppBootstrap.AppProfile.WebApp).build().login(request.getParameter("user"), request.getParameter("password"));
                // login ok!
                final HttpSession session = request.getSession();
                if (null != (GuiceBean) session.getAttribute(WebBootstrap.littleGuice)) {
                    // already logged in as another user - clear out that session, and create a new one
                    logout(request);
                    // create a new session
                    //session = request.getSession( true );
                    //throw new IllegalStateException("Session already logged in");
                }
                // BUSTED!!! WebBootstrap.bootstrap(boot, session);
                final String forwardURL;
                {
                    final String param = request.getParameter("okURL");
                    forwardURL = (null != param) ? param : getLoginOkURL();
                }

                try {
                    response.sendRedirect(request.getContextPath() + forwardURL);
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

                final String forwardURL;
                {
                    String param =  request.getParameter("failedURL");
                    forwardURL = (param != null) ? param : getLoginFailedURL();
                }
                request.setAttribute("exception", ex);
                try {
                    /*
                    response.getWriter().println("<html><head><meta http-equiv=\"refresh\" content=\"2;url="
                    + request.getContextPath() + forwardURL
                    + "\"/></head><body>OK ... <a href=\""
                    + request.getContextPath() + forwardURL + "\">redirecting ...</a>"
                    + "</body></html>");
                    //response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
                    //response.setHeader("Location", "http://yahoo.com");
                    //getServletConfig().getServletContext().getRequestDispatcher(forwardURL).include(request, response);
                     *
                     */
                    response.sendRedirect(request.getContextPath() + forwardURL);
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
                    /*...
                    response.getWriter().println("<html><head><meta http-equiv=\"refresh\" content=\"2;url="
                    + request.getContextPath() + forwardURL
                    + "\"/></head><body>OK ... <a href=\""
                    + request.getContextPath() + forwardURL + "\">redirecting ...</a>"
                    + "</body></html>");

                    //response.getWriter().println( "<html><head></head><body>OK</body></html>" );
                    //response.setStatus( HttpServletResponse.SC_MOVED_TEMPORARILY );
                    //response.setHeader( "Location", "http://yahoo.com" );
                    //getServletConfig().getServletContext().getRequestDispatcher(forwardURL).forward(request, response);
                     */
                    response.sendRedirect(request.getContextPath() + forwardURL);
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
        sessionDestroyed(
                session);

        // Just unbind all the data in the session!
        for (Enumeration<String> scan = session.getAttributeNames();
                scan.hasMoreElements();) {
            session.removeAttribute(scan.nextElement());
        } //session.invalidate();
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
        } catch (IllegalStateException ex) {
            log.log(Level.INFO, "Ignoring webapp state exception setting up session - some weird glassfish race condition", ex);
        }
        chain.doFilter(request, response);
    }
}
