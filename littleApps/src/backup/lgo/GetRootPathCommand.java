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

import littleware.lgo.AbstractLgoCommand;
import littleware.lgo.LgoArgException;
import littleware.lgo.LgoException;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import littleware.asset.AssetPath;
import littleware.asset.AssetPathFactory;
import littleware.base.Whatever;
import littleware.base.feedback.Feedback;

/**
 * Command tries to resolve home-rooted path for given asset-path.
 * See AssetPathFactory.toRootedPath
 */
public class GetRootPathCommand extends AbstractLgoCommand<AssetPath,AssetPath> {
    private AssetPathFactory pathFactory;

    @Inject
    public GetRootPathCommand(
            AssetPathFactory pathFactory
            ) {
        super( GetRootPathCommand.class.getName(), null );
        this.pathFactory = pathFactory;
    }

    private enum Option { path }


    @Override
    public AssetPath runCommand(Feedback feedback ) throws LgoException {
        final AssetPath pathIn = getInput();
        final Map<String,String> mapOptions = new HashMap<String,String>();
        mapOptions.put( Option.path.toString(), (null != pathIn) ? pathIn.toString() : null );

        final  Map<String,String>  mapArgs = null; //processArgs( mapOptions, getArgs() );
        final  String  sPath = mapArgs.get( Option.path.toString() );

        if ( Whatever.get().empty(sPath)) {
            throw new LgoArgException ( "Must specify path to asset to delete" );
        }
        final AssetPath   path;
        try {
            path = pathFactory.createPath( sPath );
        } catch ( Exception ex ) {
            throw new LgoArgException( "Unable to parse path: " + sPath, ex );
        }
        try {
            return pathFactory.toRootedPath(path);
        } catch (Exception ex) {
            throw new LgoArgException( "Unable to resolve root path: " + path, ex );
        }
    }

}
