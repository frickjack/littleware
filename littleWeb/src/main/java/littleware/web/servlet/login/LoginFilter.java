/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.web.servlet.login;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.logging.Logger;
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import littleware.base.Options;
import littleware.base.Whatever;
import littleware.web.beans.GuiceBean;
import littleware.web.servlet.WebBootstrap;
import littleware.web.servlet.helper.*;
import littleware.web.servlet.login.controller.*;

/**
 * Works in conjunction with LoginServlet to setup authenticated (or not)
 * littleware environment that servlets or whatever can access via
 * session.getAttribute( "guiceBean" ). This filter assumes javascript clients
 * that will respond properly to json response indicating that authentication is
 * required.
 */
public class LoginFilter implements Filter {

    public static final String guiceBean = WebBootstrap.littleGuice;
    public static final String littleCookie = SessionMgr.littleCookieName;
    private static final Logger log = Logger.getLogger(LoginFilter.class.getName());

    @Singleton
    public static class Tools {

        final SessionMgr mgr;
        final ResponseHelper helper;
        final Provider<JsonResponse.Builder> respFactory;

        @Inject()
        public Tools(SessionMgr mgr, ResponseHelper helper, Provider<JsonResponse.Builder> respFactory) {
            this.mgr = mgr;
            this.helper = helper;
            this.respFactory = respFactory;

        }
    }

    private Tools tools;
    private final Gson gsonTool = new Gson();
    private final MimetypesFileTypeMap mimeMap = new MimetypesFileTypeMap();

    /**
     * Initialization parameter - whether the filter should require
     * authenticated sessions, or also accept unauthenticated sessions.
     */
    public boolean authRequired = false;
    
    public LoginFilter() {}
    
    @Inject()
    public LoginFilter( Tools tools ) {
        this.tools = tools;
    }

    /**
     * Sets up guice environment, and queries "authRequired" initialization
     * parameter
     *
     * @param fc
     * @throws ServletException
     */
    @Override
    public void init(FilterConfig fc) throws ServletException {
        // noop
        final GuiceBean gbean = (GuiceBean) fc.getServletContext().getAttribute(WebBootstrap.littleGuice);
        Whatever.get().check("Application scope guice bean initialized", gbean != null);
        this.tools = gbean.getInstance(Tools.class);

        final String authStr = Options.some(fc.getInitParameter("authRequired")).getOr("false").trim().toLowerCase();
        authRequired = authStr.equals("true") || authStr.equals("yes");
    }
        
    
    /**
     * Just make sure the freakin' littleware session is initialized ..., refuse
     * to allow unauthenticated sessions to access resource if authRequired init
     * parameter set, otherwise just auto-setup uninitialized sessions when
     * needed.
     */
    @Override
    public void doFilter(ServletRequest requestIn, ServletResponse responseIn, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest req = (HttpServletRequest) requestIn;
        final HttpServletResponse resp = (HttpServletResponse) responseIn;
        final ImmutableList<Cookie>  cookies;
        
        {
            final Cookie[] temp = req.getCookies();
            
            if(null != temp) {
                cookies = ImmutableList.copyOf( temp );
            } else cookies = ImmutableList.of();
        }
        
        final SessionMgr.AuthKey key = tools.mgr.authorizeRequest( cookies, Options.some( req.getHeader( "little-sessionId")));
        
        if ( key.addCookie ) {
            tools.mgr.addSessionCookie(req, resp, key.info);
        }
        if ( key.authenticated || (! authRequired) ) {
            req.setAttribute(WebBootstrap.littleGuice, key.info.getGBean());
            chain.doFilter(req, resp);
        } else {
            // Send the client some JSON telling them to authenticate
            final JsonResponse jsr = tools.respFactory.get().status.set(HttpServletResponse.SC_UNAUTHORIZED).build();
            tools.helper.write(resp, jsr);
        }
    }

    @Override
    public void destroy() {
    }
}
