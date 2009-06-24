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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import littleware.apps.client.UiFeedback;
import littleware.asset.AssetPath;
import littleware.asset.AssetPathFactory;
import littleware.asset.AssetSearchManager;
import littleware.base.Whatever;

/**
 * List the child assets under a given asset path by id,name
 */
public class ListChildrenCommand extends AbstractLgoCommand<String,Map<String,UUID>> {
    private final AssetSearchManager osearch;
    private final AssetPathFactory   ofactoryPath;

    @Inject
    public ListChildrenCommand( AssetSearchManager search,
            AssetPathFactory factoryPath
            ) {
        super( ListChildrenCommand.class.getName() );
        osearch = search;
        ofactoryPath = factoryPath;
    }

    /**
     * Run with resolved path
     *
     * @param feedback
     * @param sPath resolved from args, etc - empty check applied
     * @return
     * @throws littleware.apps.lgo.LgoException
     */
    private Map<String,UUID> runInternal( UiFeedback feedback, AssetPath path ) throws LgoException {
        final Map<String,UUID> mapChildren;
        try {
            mapChildren = osearch.getAssetIdsFrom(
                    osearch.getAssetAtPath(path).get().getObjectId(),
                    null
                    );
        } catch ( Exception ex ) {
            throw new LgoException( "Unable to access children under " + path, ex );
        }

        return mapChildren;
    }
    
    /**
     * Return the path resolved from path
     * 
     * @return path resolved from args and default
     * @throws littleware.apps.lgo.LgoArgException
     */
    private AssetPath getPathFromArgs ( String sDefaultPath ) throws LgoException {
        final String sPathOption = "path";
        final Map<String,String> mapOpt = new HashMap<String,String>();
        mapOpt.put( sPathOption, sDefaultPath );
        final String sPath = processArgs( mapOpt, getArgs() ).get( sPathOption );
        if ( Whatever.empty( sPath ) ) {
            throw new LgoArgException( "Must specify path to list children under" );
        }
        try {
            return ofactoryPath.createPath(sPath);
        } catch ( Exception ex ) {
            throw new LgoArgException( "Unable to construct path: " + sPath );
        }
    }

    /**
     * List the children under the -path argument,
     * or sDefaultPath if -path not given
     * 
     * @param feedback
     * @param sDefaultPath if -path argument not given
     * @return id,path; id,path; ...
     * @throws littleware.apps.lgo.LgoException
     */
    @Override
    public Map<String,UUID> runSafe(UiFeedback feedback, String sDefaultPath ) throws LgoException {
        return runInternal( feedback, getPathFromArgs( sDefaultPath ) );
    }

    @Override
    public String runCommandLine( UiFeedback feedback, String sDefaultPath ) throws LgoException {
        final AssetPath path = getPathFromArgs( sDefaultPath );
        final Map<String,UUID> mapChildren = runInternal( feedback, path );
        final List<String> vChildren = new ArrayList<String>( mapChildren.keySet() );
        Collections.sort( vChildren );
        final StringBuilder sb = new StringBuilder();
        for( String sChild : vChildren ) {
            sb.append( mapChildren.get( sChild ).toString() ).append( "," ).
                    append( path.toString() ).append( "/" ).append( sChild ).
                    append( Whatever.NEWLINE );
        }
        return sb.toString();
    }

}
