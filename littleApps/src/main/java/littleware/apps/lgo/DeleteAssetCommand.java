/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.lgo;

import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetManager;
import littleware.asset.AssetPath;
import littleware.asset.AssetPathFactory;
import littleware.asset.AssetSearchManager;
import littleware.base.Whatever;
import littleware.base.feedback.Feedback;

/**
 * Simple delete-asset command - refuses to delete asset
 * that has child assets linking from it.
 * Takes single string argument that specifies the AssetPath to
 * delete:
 *      lgo delete -path [path] -comment [delete_comment]
 */
public class DeleteAssetCommand extends AbstractLgoCommand<String,UUID> {
    private AssetSearchManager osearch;
    private AssetPathFactory opathFactory;
    private AssetManager omgrAsset;

    @Inject
    public DeleteAssetCommand( AssetSearchManager search,
            AssetPathFactory pathFactory,
            AssetManager     mgrAsset
            ) {
        super( DeleteAssetCommand.class.getName() );
        osearch = search;
        opathFactory = pathFactory;
        omgrAsset = mgrAsset;
    }

    private enum Option { path, comment; }

    @Override
    public UUID runSafe(Feedback feedback, String sPathIn ) throws LgoException {

        final Map<String,String> mapOptions = new HashMap<String,String>();
        mapOptions.put( Option.path.toString(), sPathIn );
        mapOptions.put( Option.comment.toString(), "No comment" );

        final  Map<String,String>  mapArgs = processArgs( mapOptions, getArgs() );
        final  String  sPath = mapArgs.get( Option.path.toString() );
        final  String  sComment = mapArgs.get( Option.comment.toString() );

        if ( Whatever.get().empty(sPath)) {
            throw new LgoArgException ( "Must specify path to asset to delete" );
        }
        final AssetPath   path;
        try {
            path = opathFactory.createPath( sPath );
        } catch ( Exception ex ) {
            throw new LgoArgException( "Unable to parse path: " + sPath, ex );
        }
        final Asset    aDelete;
        try {
            aDelete = osearch.getAssetAtPath(path).get();
        } catch ( Exception ex ) {
            throw new LgoException( "Could not load asset at path: " + sPath, ex );
        }
        try {
            if ( ! osearch.getAssetIdsFrom( aDelete.getId(), null ).isEmpty() ) {
                throw new LgoArgException( "May not delete asset with child assets linking from it: " + sPath );
            }
        } catch ( LgoException ex ) {
            throw ex;
        } catch ( Exception ex ) {
            throw new LgoException( "Failed to load info on asset children: " + sPath, ex );
        }
        try {
            omgrAsset.deleteAsset( aDelete.getId(), sComment );
        } catch ( Exception ex ) {
            throw new LgoException( "Server delete of " + sPath + " failed", ex );
        }
        return aDelete.getId();
    }

}
