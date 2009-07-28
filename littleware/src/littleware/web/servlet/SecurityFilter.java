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
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.IOException;
import javax.security.auth.Subject;
import java.security.*;

import littleware.web.beans.*;
import littleware.security.auth.*;
import littleware.security.LittleUser;
import littleware.security.TomcatUser;
import littleware.asset.AssetRetriever;
import littleware.asset.AssetException;
import littleware.base.BaseException;
import littleware.base.AssertionFailedException;

/**
 * Little filter that sets the javax.security.auth.subject
 * attribute appropriately so that Tomcat runs servlets
 * as the appropriate authenticated user.
 * Also establishes link between Tomcat JAAS Realm and Littleware 
 * JAAS LoginContext setups.
 */
public class SecurityFilter implements Filter {

    private static Logger olog_generic = Logger.getLogger("littleware.web.servlet.SecurityFilter");

    /** Do nothing constructor */
    public SecurityFilter() {
    }

    /** Do nothing init */
    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    /** Do nothing */
    @Override
    public void destroy() {
    //config = null;
    }

    /**
     * Privileged action to run servlet as our authenticated Subject
     */
    private static class PrivilegedChain implements PrivilegedExceptionAction<Object> {

        private FilterChain ohttp_chain = null;
        private HttpServletRequest ohttp_request = null;
        private HttpServletResponse ohttp_response = null;

        /**
         * Stash the args to chain off with
         */
        public PrivilegedChain(FilterChain http_chain,
                HttpServletRequest http_request,
                HttpServletResponse http_response) {
            ohttp_chain = http_chain;
            ohttp_request = http_request;
            ohttp_response = http_response;
        }

        /**
         * Just invoke the filterchain doFilter
         */
        @Override
        public Object run() throws IOException, ServletException {
            ohttp_chain.doFilter(ohttp_request, ohttp_response);
            return null;
        }
    }

    /**
     * Lookup the littleware.web.beans.SessionBean from the &quot;lw_user&quot;
     * session attribute, and extract the Subject to set
     * the javax.security.auth.subject session attribute to.
     */
    @Override
    public void doFilter(ServletRequest sreq, ServletResponse sres,
            FilterChain chain) throws IOException, ServletException {
        HttpServletResponse http_response = (HttpServletResponse) sres;
        HttpServletRequest http_request = (HttpServletRequest) sreq;

        HttpSession http_session = http_request.getSession(true);
        Subject subject_ready = (Subject) http_session.getAttribute("javax.security.auth.subject");

        olog_generic.log(Level.FINE, "SecurityFilter running ...");
        Principal p_user = http_request.getUserPrincipal();

        if ((subject_ready == null) || subject_ready.getPrincipals(LittleUser.class).isEmpty()) {
            subject_ready = null;
            SessionBean bean_session = (SessionBean) http_session.getAttribute("lw_user");

            if ( bean_session != null ) {
                SessionHelper m_helper = bean_session.getHelper();
                try {
                    AssetRetriever m_search = m_helper.getService(ServiceType.ASSET_SEARCH);
                    subject_ready = m_helper.getSession().getSubject(m_search);

                    http_session.setAttribute("javax.security.auth.subject", subject_ready);
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    olog_generic.log(Level.WARNING, "Failed to load subject for session", e );
                    throw new ServletException("Failed to load subject for session ", e);
                }
            } else if ((p_user != null) && (p_user instanceof TomcatUser)) {
                if (null == bean_session) {
                    bean_session = new SessionBean();
                    http_session.setAttribute("lw_user", bean_session);
                }

                /**
                 * Work within the Tomcat framework!
                 * We can get our hands on a Principal, but not on a javax.security.auth.Subject.  Frick.
                 */
                TomcatUser p_tomcat = (TomcatUser) p_user;
                try {
                    bean_session.setHelper(p_tomcat.getHelper());
                } catch (Exception e) {
                    throw new AssertionFailedException("Failed to setup session for user: " + p_tomcat.getName(), e);
                }
            } else {
                olog_generic.log(Level.FINE, "Unable to retrieve lw_user attribute from session");
            /*... for debugging only ...
            Enumeration enum_param = http_session.getAttributeNames ();
            while ( enum_param.hasMoreElements () ) {
            String s_name = (String) enum_param.nextElement ();
            olog_generic.log ( Level.INFO, "attribute: " + s_name );
            }
             */
            }
        }

        if (null == subject_ready) {
            chain.doFilter(http_request, http_response);
        } else {
            try {
                Subject.doAs(subject_ready,
                        new PrivilegedChain(chain, http_request, http_response));
            } catch (PrivilegedActionException e) {
                throw new ServletException("Something went wrong in the chain, caught: " + e, e);
            }
        }
    }
}

