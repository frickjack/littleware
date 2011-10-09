/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.client.internal;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import littleware.asset.Asset;
import littleware.asset.client.AssetRef;
import littleware.asset.client.AssetSearchManager;
import littleware.asset.client.AssetTreeTool;
import littleware.base.feedback.Feedback;
import littleware.base.feedback.NullFeedback;
import littleware.base.BaseException;
import littleware.base.TooMuchDataException;

/**
 * Simple AssetTreeTool implementation just calls through to
 * AssetSearchManager.
 */
@Singleton
public class SimpleAssetTreeTool implements AssetTreeTool {
    private static final Logger log = Logger.getLogger( SimpleAssetTreeTool.class.getName() );
    private static final int    MaxAsset = 1000;

    private final AssetSearchManager search;

    @Inject
    public SimpleAssetTreeTool(AssetSearchManager search) {
        this.search = search;
    }

    @Override
    public List<Asset> loadBreadthFirst(UUID uRoot, Feedback feedback, int iMaxDepth) throws BaseException, GeneralSecurityException, RemoteException {
        final List<UUID> scanList = new ArrayList<UUID>();

        scanList.add(uRoot);
        feedback.setProgress(0);
        feedback.info("Scanning node tree under " + uRoot);
        for (int i = 0; i < scanList.size(); ++i) {
            final UUID uScan = scanList.get(i);
            scanList.addAll(search.getAssetIdsFrom(uScan, null).values());
            feedback.setProgress(i, scanList.size());

            if ( scanList.size() > MaxAsset ) {
                throw new TooMuchDataException();
            }
        }
        feedback.info("Loading " + scanList.size() + " assets under tree");
        
        final Map<UUID,AssetRef> serverResult =  search.getAssets(scanList);
        final ImmutableList.Builder<Asset> resultBuilder = ImmutableList.builder();
        for( UUID id : scanList ) {
            final AssetRef ref = serverResult.get( id );
            if ( null != ref && ref.isSet() ) {
                resultBuilder.add( ref.get() );
            }
        }
        return resultBuilder.build();
    }

    @Override
    public List<Asset> loadBreadthFirst(UUID uRoot) throws BaseException, GeneralSecurityException, RemoteException {
        return loadBreadthFirst(uRoot, new NullFeedback() );
    }

    @Override
    public List<Asset> loadBreadthFirst(UUID uRoot, int iMaxDepth) throws BaseException, GeneralSecurityException, RemoteException, TooMuchDataException {
        return loadBreadthFirst(uRoot, new NullFeedback(), iMaxDepth );
    }

    @Override
    public List<Asset> loadBreadthFirst(UUID uRoot, Feedback feedback) throws BaseException, GeneralSecurityException, RemoteException, TooMuchDataException {
        return loadBreadthFirst(uRoot, feedback, MaxAsset );
    }
}
