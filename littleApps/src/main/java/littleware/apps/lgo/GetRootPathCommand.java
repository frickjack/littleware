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
        super( GetRootPathCommand.class.getName() );
        this.pathFactory = pathFactory;
    }

    private enum Option { path }

    @Override
    public AssetPath runDynamic( Feedback feedback, Object x ) throws LgoException {
       if ( (x == null) || (x instanceof AssetPath) ) {
           return runSafe( feedback, (AssetPath) x );
       } else if ( (x instanceof String) && (! ((String) x).isEmpty()) ) {
           final String s = (String) x;
           try {
               return runSafe( feedback, pathFactory.createPath( s ) );
           } catch ( LgoException ex ) {
               throw ex;
           } catch ( RuntimeException ex ) {
               throw ex;
           } catch ( Exception ex ) {
               throw new LgoArgException( "Failed to parse path " + s, ex );
           }
       } else {
           return runSafe( feedback, null );
       }
    }

    @Override
    public AssetPath runSafe(Feedback feedback, AssetPath pathIn ) throws LgoException {

        final Map<String,String> mapOptions = new HashMap<String,String>();
        mapOptions.put( Option.path.toString(), (null != pathIn) ? pathIn.toString() : null );

        final  Map<String,String>  mapArgs = processArgs( mapOptions, getArgs() );
        final  String  sPath = mapArgs.get( Option.path.toString() );

        if ( Whatever.empty(sPath)) {
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
