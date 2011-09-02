/*
 * Copyright 2011 http://code.google.com/p/littleware
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.web.servlet;

import com.google.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.logging.Logger;
import java.io.IOException;

import java.util.logging.Level;
import littleware.asset.AssetPath;
import littleware.asset.AssetPathFactory;
import littleware.asset.client.AssetSearchManager;
import littleware.base.AssertionFailedException;
import littleware.base.Maybe;
import littleware.base.Option;
import littleware.security.AccountManager;
import littleware.security.LittleAcl;
import littleware.security.LittleGroup;
import littleware.security.LittlePermission;
import littleware.security.LittleUser;
import littleware.web.beans.*;

/**
 * Little filter that sets the javax.security.auth.subject
 * attribute appropriately so that Tomcat runs servlets
 * as the appropriate authenticated user.
 * Also establishes link between Tomcat JAAS Realm and Littleware 
 * JAAS LoginContext setups.
 */
public class SecurityFilter implements Filter {

    private static final Logger log = Logger.getLogger(SecurityFilter.class.getName());
    private String loginFormUri = "/home.jsf";
    private Option<String> accessControl = Maybe.empty();

    /** Do nothing init */
    @Override
    public void init(FilterConfig config) throws ServletException {
        final String uri = config.getInitParameter("loginForm");
        if (null != uri) {
            if (uri.startsWith("/")) {
                loginFormUri = uri;
            } else {
                loginFormUri = "/" + uri;
            }
        }
        log.log(Level.INFO, "Login URI set to : " + loginFormUri);

        final String accessCheck = config.getInitParameter("accessControl");
        if (null == accessCheck) {
            log.log(Level.INFO, "No access check registered");
        } else if (accessCheck.startsWith("group:") || accessCheck.startsWith("acl:read:") || accessCheck.startsWith("acl:write:")) {
            accessControl = Maybe.something(accessCheck);
            log.log(Level.INFO, "Access control registered: " + accessCheck);
        } else {
            log.log(Level.WARNING, "Ignoring illegal access control: " + accessCheck);
        }
    }

    /** Do nothing */
    @Override
    public void destroy() {
    }

    /**
     * Internal utility class - inject properties, then check access
     */
    public static class AccessControl extends InjectMeBean {

        private AssetSearchManager search;
        private LittleUser user;
        private AssetPathFactory pathFactory;
        private LittleGroup adminGroup;

        @Inject
        public void injectMe(AssetSearchManager search, LittleUser user, AssetPathFactory pathFactory) {
            this.search = search;
            this.user = user;
            this.pathFactory = pathFactory;
            try {
                this.adminGroup = search.getAsset( AccountManager.UUID_ADMIN_GROUP ).get().narrow();
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new AssertionFailedException("Failed to load admin group", ex);
            }
        }

        /**
         * Return true if access allowed
         * 
         * @param sAccessSpec of form group:groupName or
         *    acl:(read|write):aclName
         */
        public boolean checkAccess(String sAccessSpec) {
            try {
                if ( adminGroup.isMember( user ) ) {
                    return true;
                }
                if (sAccessSpec.startsWith("group:/")) {
                    final AssetPath path = pathFactory.createPath(sAccessSpec.substring("group:".length()));
                    return search.getAssetAtPath(path).get().narrow(LittleGroup.class).isMember(user);
                } else if (sAccessSpec.startsWith("acl:read:/")) {
                    final AssetPath path = pathFactory.createPath(sAccessSpec.substring("acl:read:".length()));
                    return search.getAssetAtPath(path).get().narrow(LittleAcl.class).checkPermission(user, LittlePermission.READ);
                } else if (sAccessSpec.startsWith("acl:write:/")) {
                    final AssetPath path = pathFactory.createPath(sAccessSpec.substring("acl:write:".length()));
                    return search.getAssetAtPath(path).get().narrow(LittleAcl.class).checkPermission(user, LittlePermission.WRITE);
                } else if (sAccessSpec.startsWith("group:")) {
                    final String group = sAccessSpec.substring("group:".length());
                    return search.getByName(group, LittleGroup.GROUP_TYPE).get().narrow(LittleGroup.class).isMember(user);
                } else if (sAccessSpec.startsWith("acl:read:")) {
                    final String acl = sAccessSpec.substring("acl:read:".length());
                    return search.getByName(acl, LittleAcl.ACL_TYPE).get().narrow(LittleAcl.class).checkPermission(user, LittlePermission.READ);
                } else if (sAccessSpec.startsWith("acl:write:")) {
                    final String acl = sAccessSpec.substring("acl:write:".length());
                    return search.getByName(acl, LittleAcl.ACL_TYPE).get().narrow(LittleAcl.class).checkPermission(user, LittlePermission.WRITE);
                } else {
                    log.log(Level.WARNING, "Unknown access spec must be in (group:, acl:read:, acl:write:): " + sAccessSpec);
                    return false;
                }
            } catch (Exception ex) {
                log.log(Level.WARNING, "Access check against " + sAccessSpec + " failed for " + user, ex);
            }
            return false;
        }
    }

    private void redirectToLogin(ServletRequest sreq, ServletResponse sres) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) sreq;
        final HttpServletResponse response = (HttpServletResponse) sres;
        final String uri = request.getRequestURI();
        final String context = request.getContextPath();
        final String contextRelativeUri;
        if ((!context.isEmpty()) && uri.startsWith(context)) {
            contextRelativeUri = uri.substring(context.length());
        } else {
            contextRelativeUri = uri;
        }
        String redirect = context + loginFormUri;
        if (redirect.indexOf('?') > 0) {
            redirect += "&loginTrigger=" + contextRelativeUri;
        } else {
            redirect += "?loginTrigger=" + contextRelativeUri;
        }
        response.sendRedirect(redirect);
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
        final HttpSession session = request.getSession(true);
        final GuiceBean bean = (GuiceBean) session.getAttribute(WebBootstrap.littleGuice);

        if ((null == bean) || (!bean.isLoggedIn())) {
            redirectToLogin(sreq, sres);
            return;
        } else if (accessControl.isSet()) {
            final AccessControl check = new AccessControl();
            check.setGuiceBean(bean);
            if (!check.checkAccess(accessControl.get())) {
                redirectToLogin(sreq, sres);
                return;
            }
        }
        // if here - then logged in and access control ok
        chain.doFilter(sreq, sres);
    }
}
