/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.web.servlet;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import littleware.asset.Asset;
import littleware.asset.gson.LittleGsonFactory;
import littleware.asset.internal.RemoteSearchManager;
import littleware.asset.server.LittleContext;
import littleware.base.Maybe;
import littleware.base.Option;
import littleware.base.UUIDFactory;
import littleware.security.AccessDeniedException;

/**
 *
 * @author pasquini
 */
public class AssetSearchServlet extends HttpServlet {
    private static final Logger log = Logger.getLogger( AssetSearchServlet.class.getName() );
    
    private String uriPrefix = "/asset/services";

    public static class Tools {

        private final LittleGsonFactory gsonFactory;
        private final RemoteSearchManager search;
        private final LittleContext.ContextFactory ctxFactory;

        /**
         * Injectable tools
         */
        public Tools(LittleGsonFactory gsonFactory,
                RemoteSearchManager search,
                LittleContext.ContextFactory ctxFactory) {
            this.gsonFactory = gsonFactory;
            this.search = search;
            this.ctxFactory = ctxFactory;
        }

        public LittleGsonFactory getGsonFactory() {
            return gsonFactory;
        }

        public RemoteSearchManager getSearchMgr() {
            return search;
        }

        public LittleContext.ContextFactory getCtxFactory() {
            return ctxFactory;
        }
    }
    private Tools tools = null;

    @Override
    public void init() {
        final ServletConfig config = getServletConfig();
        uriPrefix = Maybe.emptyIfNull(config.getInitParameter("uriPrefix")).getOr(uriPrefix);
        // TODO - work out bootstrap/injection mechanism
        tools = null;  // config.getServletContext().getAttribute( "guiceBean" );
    }
    final Pattern uriPattern = Pattern.compile(".*/asset/(\\w+)/([^/]+).*");

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final Matcher matcher = uriPattern.matcher(request.getRequestURI().substring(uriPrefix.length()));
        if (!matcher.matches()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        final String method = matcher.group(1).toLowerCase();
        final String arguments = matcher.group(2);
        final UUID sessionId;
        final long vtInClientCache;
        {
            final String sessionIdStr = request.getParameter("sessionId");
            if (null == sessionIdStr) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            sessionId = UUIDFactory.parseUUID(sessionIdStr);
        }
        {
            final String vtStr = request.getParameter( "cacheTimeStamp" );
            if ( null != vtStr ) {
                vtInClientCache = Long.parseLong( vtStr );
            } else {
                vtInClientCache = -1L;
            }
        }
        if (method.equals("withId")) {
            final UUID id = UUIDFactory.parseUUID(arguments);
            final Option<Asset> maybe;
            try {
                maybe = tools.search.getAsset(sessionId, id);
            } catch ( AccessDeniedException ex ) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;                
            } catch ( GeneralSecurityException ex ) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;                                
            } catch (Exception ex) {
                // TODO!!!
                return; 
            }
            if ( maybe.isEmpty() ) {
                response.setStatus( HttpServletResponse.SC_NOT_FOUND );
                return;
            } 
            //
            // NOTE - TODO - generalize RemoteSearchManager interface to support client cache directly
            //
            final Asset asset = maybe.get();
            if ( vtInClientCache == asset.getTimestamp() ) {
                response.setStatus( HttpServletResponse.SC_NO_CONTENT );
                return;
            }
            response.setContentType( "application/json" );
            response.getWriter().write(
                    tools.gsonFactory.get().toJson( asset, Asset.class )
                    );
        } else {
            response.sendError( HttpServletResponse.SC_NOT_IMPLEMENTED );
        }
    }
}
