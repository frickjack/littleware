/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.webproxy;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
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
import littleware.asset.internal.RemoteSearchManager.AssetResult;
import littleware.asset.server.LittleContext;
import littleware.base.Options;
import littleware.base.UUIDFactory;
import littleware.bootstrap.SessionInjector;
import littleware.security.AccessDeniedException;

/**
 * RemoteSearchManager REST service
 */
public class AssetSearchServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(AssetSearchServlet.class.getName());
    private String uriPrefix = "/littleware/services/search";

    public static class Tools {

        private final LittleGsonFactory gsonFactory;
        private final RemoteSearchManager search;

        /**
         * Injectable tools
         */
        @Inject
        public Tools(LittleGsonFactory gsonFactory,
                RemoteSearchManager search) {
            this.gsonFactory = gsonFactory;
            this.search = search;
        }

        public LittleGsonFactory getGsonFactory() {
            return gsonFactory;
        }

        public RemoteSearchManager getSearchMgr() {
            return search;
        }
    }
    private Tools tools = null;

    /**
     * In-container constructor
     */
    public AssetSearchServlet() {
    }

    @Inject
    public AssetSearchServlet(Tools tools) {
        this.tools = tools;
    }

    @Override
    public void init() {
        final ServletConfig config = getServletConfig();
        uriPrefix = Options.some(config.getInitParameter("uriPrefix")).getOr(uriPrefix);
        // TODO - work out bootstrap/injection mechanism
        if (null == tools) {
            tools = ((SessionInjector) config.getServletContext().getAttribute("injector")).getInstance(Tools.class);
        }
    }
    private final Pattern uriPattern = Pattern.compile("/(\\w+)/([^/]+).*");
    private final TypeToken<Map<String, UUID>> name2IdToken = new TypeToken<Map<String, UUID>>() {
    };

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final Gson gson = tools.gsonFactory.getBuilder().setPrettyPrinting().create();
        final Matcher matcher = uriPattern.matcher(request.getRequestURI().substring(uriPrefix.length()));
        if (!matcher.matches()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Unexpected URI: " + request.getRequestURI() );
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
        { // should maybe move this to a header ... ?
            final String vtStr = request.getParameter("cacheTimeStamp");
            if (null != vtStr) {
                vtInClientCache = Long.parseLong(vtStr);
            } else {
                vtInClientCache = -1L;
            }
        }
        if (method.equals("withId")) {
            final UUID id = UUIDFactory.parseUUID(arguments);
            final AssetResult result;
            try {
                // TODO - update to accept clientTStamp parameter ...
                result = tools.search.getAsset(sessionId, id, vtInClientCache);
            } catch (AccessDeniedException ex) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            } catch (GeneralSecurityException ex) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            } catch (Exception ex) {
                // TODO!!!
                log.log(Level.WARNING, "Unexpected exception", ex);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.toString());
                return;
            }
            switch (result.getState()) {
                case NO_SUCH_ASSET: {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
                break;
                case ACCESS_DENIED: {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
                break;
                case USE_YOUR_CACHE: {
                    response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                    if (!response.containsHeader("Date")) {
                        response.setDateHeader("Date", (new Date()).getTime());
                    }
                }
                break;
                default: {
                    response.setContentType("application/json");
                    response.getWriter().write(gson.toJson(result.getAsset().get(), Asset.class));
                }
                break;
            }
            return;
        } else if (method.equals("roots")) {
            try {
                final Map<String, UUID> name2IdMap = tools.search.getHomeAssetIds(sessionId);
                response.setContentType("application/json");
                response.getWriter().write(gson.toJson(name2IdMap, name2IdToken.getType()));
            } catch (Exception ex) {
                // TODO!!!
                log.log(Level.WARNING, "Unexpected exception", ex);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.toString());
                return;
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
        }
    }
}
