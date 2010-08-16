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

import littleware.lgo.LgoArgException;
import littleware.lgo.LgoException;
import com.google.inject.Inject;
import littleware.asset.Asset;
import littleware.asset.AssetPath;
import littleware.asset.AssetPathFactory;
import littleware.asset.AssetSearchManager;
import littleware.asset.pickle.HumanPicklerProvider;
import littleware.base.feedback.Feedback;

/**
 * Get the asset at a given path
 */
public class GetAssetCommand extends AbstractAssetCommand<AssetPath,Asset> {
    private final AssetSearchManager osearch;
    private final AssetPathFactory ofactoryPath;

    @Inject
    public GetAssetCommand(
            HumanPicklerProvider        providePickler,
            AssetPathFactory            factoryPath,
            AssetSearchManager          search
            ) {
        super( GetAssetCommand.class.getName(), providePickler, null );
        osearch = search;
        ofactoryPath = factoryPath;
    }

    public AssetPath getPathFromArgs( Object xDefault ) throws LgoArgException {
        String sPath = null; //processArgs( getArgs(), "path" ).get( "path" );
        if ( null == sPath ) {
            sPath = (null == xDefault) ? null : xDefault.toString();
        }
        try {
            return ofactoryPath.createPath(sPath);
        } catch ( Exception ex ) {
            throw new LgoArgException( "Unable to parse path: " + sPath, ex );
        }
    }

    /**
     * Lookup the asset at the given path
     *
     * @param feedback
     * @param in path to lookup if --path argument not given
     * @return
     * @throws littleware.apps.lgo.LgoException
     */
    @Override
    public Asset runCommand(Feedback feedback) throws LgoException {
        return runInternal( feedback, getInput() );
    }


    private Asset runInternal( Feedback feedback, AssetPath in ) throws LgoException {
        try {
            return osearch.getAssetAtPath(in).get();
        } catch ( RuntimeException ex ) {
            throw ex;
        } catch ( LgoException ex ) {
            throw ex;
        } catch ( Exception ex ) {
            throw new LgoException( "Failed to retrieve asset at " + in, ex );
        }
    }

    
}

