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
import littleware.asset.Asset;
import littleware.asset.AssetManager;
import littleware.asset.AssetPath;
import littleware.asset.AssetPathFactory;
import littleware.asset.AssetSearchManager;
import littleware.asset.AssetType;
import littleware.asset.pickle.HumanPicklerProvider;
import littleware.base.Whatever;
import littleware.base.feedback.Feedback;

/**
 * Create a generic asset that inherits most properties from its parent
 */
public class CreateFolderCommand extends AbstractAssetCommand<String,Asset> {
    private final AssetSearchManager            osearch;
    private final AssetManager                  omgrAsset;
    private final AssetPathFactory              ofactoryPath;
    private final AssetType    otypeCreate;
    /**
     * Allow subtypes to specialize based on asset-type
     */
    protected CreateFolderCommand( String sName, AssetType typeCreate,
            AssetSearchManager search,
            AssetManager mgrAsset,
            AssetPathFactory factoryPath,
            HumanPicklerProvider providePickler
            )
    {
        super( sName, providePickler );
        osearch = search;
        omgrAsset = mgrAsset;
        ofactoryPath = factoryPath;
        otypeCreate = typeCreate;
    }

    @Inject
    public CreateFolderCommand ( AssetSearchManager search,
            AssetManager mgrAsset,
            AssetPathFactory factoryPath,
            HumanPicklerProvider providePickler
            ) {
        this( CreateFolderCommand.class.getName(), AssetType.GENERIC,
                search, mgrAsset, factoryPath, providePickler
                );
    }

    private enum Option { path, comment }

    /**
     * Create the folder at /parent/name,
     * where sNameIn is the default name if -name argument not given.
     *
     * @param feedback
     * @param sDefaultPath
     * @return id of new asset
     * @throws littleware.apps.lgo.LgoException
     */
    @Override
    public Asset runSafe(Feedback feedback, String sDefaultPath ) throws LgoException {
        final Map<String,String> mapDefault = new HashMap<String,String>();
        mapDefault.put( Option.path.toString(), sDefaultPath );
        mapDefault.put( Option.comment.toString(), "no comment" );

        final Map<String,String> mapArgs = processArgs( mapDefault, getArgs() );
        for ( Option opt : Option.values() ) {
            if ( Whatever.get().empty( mapArgs.get( Option.path.toString() ) ) ) {
                throw new LgoArgException( "Missing required argument: " + opt );
            }
        }
        final String    sPath = mapArgs.get( Option.path.toString() );
        final AssetPath path;
        try {
            path = ofactoryPath.createPath( sPath );
        } catch ( Exception ex ) {
            throw new LgoArgException( "Failed to parse path: " + sPath, ex );
        }
        final Asset  aParent;
        try {
            aParent = osearch.getAssetAtPath(
                path.getParent()
                ).get();
        } catch ( Exception ex ) {
            throw new LgoException ( "Failed to load parent of: " + path );
        }
        final String sComment = mapArgs.get( Option.comment.toString() );
        final Asset aNew = otypeCreate.create().parent(aParent).name( path.getBasename() ).
            comment( sComment ).build();
        try {
            return omgrAsset.saveAsset( aNew, "CreateFolderCommand" );
        } catch ( Exception ex ) {
            throw new LgoException( "Failed to save new asset " + sPath, ex );
        }
    }


}
