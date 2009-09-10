/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.web.servlet;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.logging.Logger;
import java.io.IOException;
import java.security.*;

import littleware.web.beans.*;

/**
 * Little filter that sets the javax.security.auth.subject
 * attribute appropriately so that Tomcat runs servlets
 * as the appropriate authenticated user.
 * Also establishes link between Tomcat JAAS Realm and Littleware 
 * JAAS LoginContext setups.
 */
public class SecurityFilter implements Filter {
    private static Logger log = Logger.getLogger( SecurityFilter.class.getName() );

    private String  loginFormUri = "/home.jsf";

    /** Do nothing init */
    @Override
    public void init(FilterConfig config) throws ServletException {
        final String uri = config.getInitParameter( "loginForm" );
        if ( null != uri ) {
            if ( uri.startsWith( "/" ) ) {
                loginFormUri = uri;
            } else {
                loginFormUri = "/" + uri;
            }
        }
    }

    /** Do nothing */
    @Override
    public void destroy() {
    }


    /**
     * Lookup the littleware.web.beans.SessionBean from the &quot;lw_user&quot;
     * session attribute, and extract the Subject to set
     * the javax.security.auth.subject session attribute to.
     */
    @Override
    public void doFilter(ServletRequest sreq, ServletResponse sres,
            FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) sreq;
        final String uri = request.getRequestURI();
        if ( uri.toLowerCase().indexOf( "/secure/" ) < 0 ) {
            chain.doFilter(sreq, sres);
            return;
        }
        final HttpServletResponse response = (HttpServletResponse) sres;                
        final HttpSession session = request.getSession(true);
        final GuiceBean bean = (GuiceBean) session.getAttribute(WebBootstrap.littleGuice);

        if ( (null != bean) && bean.isLoggedIn() ) {
            // logged in - all ok
            chain.doFilter(sreq, sres);
            return;
        }
        // redirect the user to login
        final String context = request.getContextPath();
        final String contextRelativeUri;
        if ( (! context.isEmpty()) && uri.startsWith( context ) ) {
            contextRelativeUri = uri.substring(context.length() );
        } else {
            contextRelativeUri = uri;
        }
        String redirect = context + loginFormUri;
        if ( redirect.indexOf( '?' ) > 0 ) {
            redirect += "&loginTrigger=" + contextRelativeUri;
        } else {
            redirect += "?loginTrigger=" + contextRelativeUri;
        }
        response.sendRedirect(redirect);
    }
}

