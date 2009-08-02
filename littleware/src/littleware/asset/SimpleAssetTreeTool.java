/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import littleware.apps.client.Feedback;
import littleware.apps.client.LoggerUiFeedback;
import littleware.apps.client.NullFeedback;
import littleware.base.BaseException;
import littleware.base.Maybe;
import littleware.base.TooMuchDataException;

/**
 * Simple AssetTreeTool implementation just calls through to
 * AssetSearchManager.
 */
@Singleton
public class SimpleAssetTreeTool implements AssetTreeTool {
    private static final Logger olog = Logger.getLogger( SimpleAssetTreeTool.class.getName() );
    private static final int    MaxAsset = 1000;

    private final AssetSearchManager osearch;

    @Inject
    public SimpleAssetTreeTool(AssetSearchManager search) {
        osearch = search;
    }

    @Override
    public List<Asset> loadBreadthFirst(UUID uRoot, Feedback feedback, int iMaxDepth) throws BaseException, GeneralSecurityException, RemoteException {
        final List<UUID> vScan = new ArrayList<UUID>();

        vScan.add(uRoot);
        feedback.setProgress(0);
        feedback.info("Scanning node tree under " + uRoot);
        for (int i = 0; i < vScan.size(); ++i) {
            final UUID uScan = vScan.get(i);
            vScan.addAll(osearch.getAssetIdsFrom(uScan, null).values());
            feedback.setProgress(i, vScan.size());

            if ( vScan.size() > MaxAsset ) {
                throw new TooMuchDataException();
            }
        }
        feedback.info("Loading " + vScan.size() + " assets under tree");
        // Load assets one at a time to take advantage of cache
        return osearch.getAssets(vScan);
    }

    @Override
    public List<Asset> loadBreadthFirst(UUID uRoot) throws BaseException, GeneralSecurityException, RemoteException {
        return loadBreadthFirst(uRoot, new LoggerUiFeedback(olog));
    }

    @Override
    public List<Asset> loadBreadthFirst(UUID uRoot, int iMaxDepth) throws BaseException, GeneralSecurityException, RemoteException, TooMuchDataException {
        return loadBreadthFirst(uRoot, new LoggerUiFeedback(olog), iMaxDepth );
    }

    @Override
    public List<Asset> loadBreadthFirst(UUID uRoot, Feedback feedback) throws BaseException, GeneralSecurityException, RemoteException, TooMuchDataException {
        return loadBreadthFirst(uRoot, feedback, MaxAsset );
    }
}