/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.demo.simpleCL;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import littleware.asset.Asset;
import littleware.asset.AssetPath;
import littleware.asset.AssetPathFactory;
import littleware.asset.AssetSearchManager;
import littleware.base.Maybe;
import littleware.base.Whatever;


/**
 * AppBuilder for simple command-line app that
 * just returns the children under some asset-path
 * specified in argv.
 */
public class SimpleCLBuilder extends AbstractBuilder {
    private static final Logger log = Logger.getLogger( SimpleCLBuilder.class.getName() );
    private final AssetPathFactory   pathFactory;
    private final AssetSearchManager search;

    @Inject
    public SimpleCLBuilder( AssetPathFactory pathFactory,
            AssetSearchManager search
            ) {
        this.pathFactory = pathFactory;
        this.search = search;
    }

    private static class Command implements Callable<String> {
        private final AssetSearchManager search;
        private final AssetPath path;

        public Command( AssetSearchManager search, AssetPath path ) {
            this.search = search;
            this.path = path;
        }

        @Override
        public String call() throws Exception {
            final Maybe<Asset> maybe = search.getAssetAtPath( path );
            final StringBuilder sb = new StringBuilder();
            if ( ! maybe.isSet() ) {
                sb.append( "No such asset: " ).append( path );
            } else {
                sb.append( "Assets under " ).append( path ).append( ": " ).
                        append( Whatever.NEWLINE );
                final List<String> children = new ArrayList<String>(
                        search.getAssetIdsFrom( maybe.get().getId() ).keySet()
                        );
                Collections.sort( children );
                for ( String child : children ) {
                    sb.append( child ).append( Whatever.NEWLINE );
                }
            }
            return sb.toString();
        }
    }

    @Override
    public Callable<String> build() {
        final String pathArg = getArgv().isEmpty() ? "/littleware.home" : getArgv().get(0);
        try {
            final AssetPath path = pathFactory.createPath( pathArg );
            return new Command( search, path );
        } catch (Exception ex) {
            throw new IllegalArgumentException( "Failed to parse path " + pathArg, ex );
        }
    }

}
