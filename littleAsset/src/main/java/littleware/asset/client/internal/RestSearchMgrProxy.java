/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.client.internal;

import com.google.common.collect.ImmutableMap;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.asset.AssetType;
import littleware.base.AssertionFailedException;
import littleware.base.BaseException;
import littleware.base.Option;

/**
 * Client-side proxy interacts with server over REST inspired HTTP protocol
 */
public class RestSearchMgrProxy implements RemoteSearchMgrProxy {

    private static final Logger log = Logger.getLogger(RestSearchMgrProxy.class.getName());
    private final HttpHelper helper;
    private final URL homeIdURL;

    @Inject
    public RestSearchMgrProxy(HttpHelper helper, @Named("littleware.rmi_host") String host) {
        this.helper = helper;
        try {
            homeIdURL = new URL("http://" + host + ":1238/littleware/services/search/roots/all");
        } catch (MalformedURLException ex) {
            throw new AssertionFailedException( "Failed to initialize REST URL", ex );
        }
    }

    @Override
    public AssetResult getAsset(UUID sessionId, UUID assetId, long cacheTimestamp) throws BaseException, GeneralSecurityException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<UUID, AssetResult> getAssets(UUID sessionId, Map<UUID, Long> idToCacheTStamp) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    private final TypeToken<Map<String, UUID>> nameIdToken = new TypeToken<Map<String, UUID>>() {};


    @Override
    public Map<String, UUID> getHomeAssetIds(UUID sessionId) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        try {
            final Map<String,UUID> result = helper.getFromJSON( new URL( homeIdURL.toString() + "?sessionId=" + sessionId.toString() ), 
                    nameIdToken.getType() 
                    ).getJsContent();
            if ( null == result ) {
                throw new RemoteException( "Failed to access data: " + homeIdURL );
            }
            return ImmutableMap.copyOf( result );
        } catch (IOException ex) {
            throw new RemoteException( "Failure handling result from " + homeIdURL, ex );
        }
    }

    @Override
    public Map<String, UUID> getAssetIdsFrom(UUID sessionId, UUID fromId, AssetType type) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<String, UUID> getAssetIdsFrom(UUID sessionId, UUID fromId) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Option<Asset> getByName(UUID sessionId, String name, AssetType type) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Asset> getAssetHistory(UUID sessionId, UUID assetId, Date start, Date end) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Option<Asset> getAssetFrom(UUID sessionId, UUID parentId, String name) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<UUID> getAssetIdsTo(UUID sessionId, UUID toId, AssetType type) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
