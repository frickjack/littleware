/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.web.servlet.asset;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import littleware.asset.Asset;
import littleware.asset.AssetInfo;
import littleware.asset.client.internal.RemoteAssetMgrProxy;
import littleware.asset.client.internal.RemoteSearchMgrProxy;
import littleware.asset.gson.LittleGsonFactory;
import littleware.asset.internal.RemoteSearchManager;
import littleware.asset.internal.RemoteSearchManager.InfoMapResult;
import static littleware.asset.internal.RemoteSearchManager.TStampResult.State.ACCESS_DENIED;
import static littleware.asset.internal.RemoteSearchManager.TStampResult.State.NO_DATA;
import static littleware.asset.internal.RemoteSearchManager.TStampResult.State.USE_YOUR_CACHE;
import littleware.base.Options;
import littleware.base.UUIDFactory;
import littleware.base.Whatever;
import littleware.security.AccessDeniedException;
import littleware.security.auth.LittleSession;
import littleware.web.servlet.LittleServlet;
import littleware.web.servlet.helper.JsonResponse;
import littleware.web.servlet.helper.ResponseHelper;

/**
 * Web service for littleware.asset CRUD services. Must register under the
 * dispatcher with "repo" pattern due to clumsy URL-parsing - ex:
 * http://bla/dispatcher/repo/roots
 */
public class AssetMgrServlet implements LittleServlet {

    private static final Logger log = Logger.getLogger(AssetMgrServlet.class.getName());
    private final LittleGsonFactory gsonFactory;
    private final RemoteSearchMgrProxy search;
    private final RemoteAssetMgrProxy  assetMgr;
    private final Pattern uriPattern = Pattern.compile(".*/repo/(\\w+)(/[^/]+)*");
    // gson helper
    private final TypeToken<Map<String, UUID>> name2IdToken = new TypeToken<Map<String, UUID>>() {
    };
    private final LittleSession userSession;
    private final Provider<JsonResponse.Builder> responseFactory;
    private final ResponseHelper helper;

    @Inject
    public AssetMgrServlet(
            LittleGsonFactory gsonFactory,
            RemoteSearchMgrProxy search,
            RemoteAssetMgrProxy assetMgr,
            LittleSession userSession,
            Provider<JsonResponse.Builder> responseFactory,
            ResponseHelper helper) {
        this.gsonFactory = gsonFactory;
        this.search = search;
        this.assetMgr = assetMgr;
        this.userSession = userSession;
        this.responseFactory = responseFactory;
        this.helper = helper;
    }

    /**
     * Little helper class for extracting data from "If-does-not-match" HTTP
     * header of form: "timestampInClientCache-sizeInClientCache"
     */
    public static class ETagInfo {

        public final long cacheTimestamp;
        public final int sizeInCache;

        private ETagInfo(long cacheTimestamp, int sizeInCache) {
            this.cacheTimestamp = cacheTimestamp;
            this.sizeInCache = sizeInCache;
        }

        private ETagInfo() {
            this.cacheTimestamp = -1L;
            this.sizeInCache = 0;
        }
        private static final Pattern etagPattern = Pattern.compile("(\\d+)(-(\\d+))?");
        private static final ETagInfo empty = new ETagInfo();

        /**
         * Return etag-data pulled from given tag if able to parse, otherwise
         * initialize to (-1,0)
         *
         * @param etag to parse
         */
        public static ETagInfo parse(String etag) {
            final Matcher matcher = etagPattern.matcher(etag);

            if (!matcher.matches()) {
                log.log(Level.WARNING, "Invalid etag: {0}", etag);
                return empty;
            } else {
                return new ETagInfo(Long.parseLong(matcher.group(1)),
                        (matcher.group(3) != null) ? Integer.parseInt(matcher.group(3)) : 0);
            }
        }
        
        @Override
        public String toString() { return "ETagInfo(" + cacheTimestamp + ", " + sizeInCache + ")"; }
    }

    private void writeResponse(HttpServletResponse response, RemoteSearchManager.TStampResult<?> result) throws IOException {

        switch (result.getState()) {
            case NO_DATA: {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
            break;
            case ACCESS_DENIED: {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
            break;
            case USE_YOUR_CACHE: {
                response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            }
            break;
            default: {
                final Gson gson = gsonFactory.getBuilder().setPrettyPrinting().create();
                final JsonResponse.Builder builder = responseFactory.get();

                if (result instanceof RemoteSearchManager.AssetResult) {
                    final RemoteSearchManager.AssetResult assetResult = (RemoteSearchManager.AssetResult) result;
                    response.setHeader("ETag", Long.toString(assetResult.getAsset().get().getTimestamp()));
                    final JsonObject json = gson.toJsonTree(assetResult.getAsset().get(), Asset.class).getAsJsonObject();
                    builder.content.set(json);
                } else {
                    final RemoteSearchManager.InfoMapResult imapResult = (InfoMapResult) result;
                    final String etag;

                    {
                        long maxTimestamp = -1;
                        for (AssetInfo info : imapResult.getData().values()) {
                            if (info.getTimestamp() > maxTimestamp) {
                                maxTimestamp = info.getTimestamp();
                            }
                        }
                        etag = "" + maxTimestamp + "-" + imapResult.getData().size();
                    }
                    response.setHeader("ETag", etag);
                    final JsonObject json = gson.toJsonTree(imapResult.getData()).getAsJsonObject();
                    builder.content.set(json);
                }
                helper.write(response, builder.build());
            }
            break;
        }
    }

    @Override
    public void doGetOrPostOrPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        final String                method;
        final ImmutableList<String> args;

        {  // extract method and args
            final Matcher matcher = uriPattern.matcher(request.getPathInfo());
            if (!matcher.matches()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Unexpected URI: " + request.getRequestURI());
                return;
            }
            method = matcher.group(1).toLowerCase();
            final ImmutableList.Builder<String> argBuilder = ImmutableList.builder();
            final String  argStr = (matcher.group(2) == null) ? "" : matcher.group(2);
            
            for( String token : argStr.split( "/+" ) ) {
                if ( ! token.isEmpty() ) {
                    argBuilder.add( token );
                }
            }
            
            args = argBuilder.build();
        }

        if ( (! method.equals( "roots" )) && args.isEmpty() ) {
            response.sendError( HttpServletResponse.SC_BAD_REQUEST, "missing URL suffix args" );
            return;
        }
        
        try {
            if (request.getMethod().equals("GET")) {
                final ETagInfo etagInfo = ETagInfo.parse(
                        Options.some(request.getHeader("If-None-Match")).getOr(""));

                if (method.equals("withid")) {
                    final UUID id = UUIDFactory.parseUUID( args.get(0) );
                    final RemoteSearchMgrProxy.AssetResult result = search.getAsset(userSession.getId(), id, etagInfo.cacheTimestamp);
                    writeResponse(response, result);
                    
                } else if (method.equals("roots")) {
                    final InfoMapResult result = search.getHomeAssetIds(userSession.getId(), etagInfo.cacheTimestamp, etagInfo.sizeInCache);
                    writeResponse(response, result);
                } else if ( method.equals("childrenof") ) {
                    final UUID id = UUIDFactory.parseUUID(args.get(0));
                    final InfoMapResult result = search.getAssetIdsFrom(userSession.getId(), id, etagInfo.cacheTimestamp, etagInfo.sizeInCache);
                    writeResponse(response, result);
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
                }
            } else if ( method.equals( "withid" ) ) { // POST, PUT, DELETE
                final UUID id = UUIDFactory.parseUUID(args.get(0));
                final Gson gson = gsonFactory.getBuilder().setPrettyPrinting().create(); 
                final JsonObject json = gson.fromJson(
                            new InputStreamReader( request.getInputStream(), Whatever.UTF8 ),
                            JsonElement.class
                            ).getAsJsonObject();
                final String comment = json.get( "comment" ).getAsString();
                
                if ( request.getMethod().equals( "DELETE" ) ) {
                    assetMgr.deleteAsset( userSession.getId(), id, comment );
                } else { // PUT or POST    
                    final Asset asset = gson.fromJson( json.get( "asset" ), Asset.class );
                    assetMgr.saveAsset( userSession.getId(), asset, comment );
                }
            } else {
                response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
            }
        } catch (AccessDeniedException ex) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } catch (GeneralSecurityException ex) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } catch (Exception ex) {
            // TODO!!!
            log.log(Level.WARNING, "Unexpected exception", ex);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.toString());
        }
    }
}
