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

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import littleware.apps.client.Feedback;
import littleware.asset.AssetPath;
import littleware.asset.AssetPathFactory;
import littleware.asset.AssetSearchManager;
import littleware.asset.AssetType;
import littleware.base.Maybe;
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
    private Map<String,UUID> runInternal( Feedback feedback, Data data ) throws LgoException {
        final Map<String,UUID> mapChildren;
        try {
            mapChildren = osearch.getAssetIdsFrom(
                    osearch.getAssetAtPath( data.getPath() ).get().getObjectId(),
                    data.getChildType().getOr(null)
                    );
        } catch ( Exception ex ) {
            throw new LgoException( "Unable to access children under " + data.getPath(), ex );
        }

        return mapChildren;
    }

    @VisibleForTesting
    public static class Data {
        private final AssetPath path;
        private final Maybe<AssetType> maybeType;
        public Data( AssetPath path, AssetType childType ) {
            this.path = path;
            this.maybeType = Maybe.emptyIfNull(childType);
        }
        public Data( AssetPath path ) {
            this( path, null );
        }

        public AssetPath getPath () {
            return path;
        }

        public Maybe<AssetType> getChildType() {
            return maybeType;
        }
    }
    
    /**
     * Return the path resolved from path
     * 
     * @return path resolved from args and default
     * @throws littleware.apps.lgo.LgoArgException
     */
    @VisibleForTesting
    public Data getDataFromArgs ( String sDefaultPath ) throws LgoException {
        final String sPathOption = "path";
        final String sTypeOption = "type";

        final Map<String,String> mapOpt = new HashMap<String,String>();
        mapOpt.put( sPathOption, sDefaultPath );
        mapOpt.put( sTypeOption, null );
        final Map<String,String> mapArgs = processArgs( mapOpt, getArgs() );
        final String sPath = mapArgs.get( sPathOption );
        final Maybe<String> maybeTypeName = Maybe.emptyIfNull( mapArgs.get( sTypeOption ) );
        if ( Whatever.empty( sPath ) ) {
            throw new LgoArgException( "Must specify path to list children under" );
        }
        Maybe<AssetType> maybeType = Maybe.empty();
        if ( maybeTypeName.isSet() && (! Whatever.empty( maybeTypeName.get())) ) {
            // lookup asset-type
            final String typeName = maybeTypeName.get().toLowerCase().trim();
            for ( AssetType scan : AssetType.getMembers() ) {
                if ( scan.toString().toLowerCase().equals( typeName )) {
                    maybeType = Maybe.something( scan );
                    break;
                }
            }
        }
        try {
            return new Data( ofactoryPath.createPath(sPath), maybeType.getOr(null) );
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
    public Map<String,UUID> runSafe(Feedback feedback, String sDefaultPath ) throws LgoException {
        return runInternal( feedback, getDataFromArgs( sDefaultPath ) );
    }

    @Override
    public String runCommandLine( Feedback feedback, String sDefaultPath ) throws LgoException {
        final Data argData = getDataFromArgs( sDefaultPath );
        final Map<String,UUID> mapChildren = runInternal( feedback, argData );
        final List<String> vChildren = new ArrayList<String>( mapChildren.keySet() );
        Collections.sort( vChildren );
        final StringBuilder sb = new StringBuilder();
        for( String sChild : vChildren ) {
            sb.append( mapChildren.get( sChild ).toString() ).append( "," ).
                    append( argData.getPath().toString() ).append( "/" ).append( sChild ).
                    append( Whatever.NEWLINE );
        }
        return sb.toString();
    }

}
