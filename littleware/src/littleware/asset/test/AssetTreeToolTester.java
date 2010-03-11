/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.test;

import com.google.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.Asset;
import littleware.asset.AssetManager;
import littleware.asset.AssetPathFactory;
import littleware.asset.AssetSearchManager;
import littleware.asset.AssetTreeTool;
import littleware.asset.AssetType;
import littleware.base.Maybe;
import littleware.test.LittleTest;

/**
 * Simple AssetTreeTool tester
 */
public class AssetTreeToolTester extends LittleTest {

    private static final Logger olog = Logger.getLogger(AssetTreeTool.class.getName());
    private final AssetTreeTool treeTool;
    private final AssetManager manager;
    private final AssetPathFactory pathFactory;
    private final AssetSearchManager search;

    @Inject
    public AssetTreeToolTester(AssetManager manager,
            AssetTreeTool treeTool,
            AssetPathFactory pathFactory,
            AssetSearchManager search
            ) {
        this.treeTool = treeTool;
        this.search = search;
        this.manager = manager;
        this.pathFactory = pathFactory;
        setName("testTreeTool");
    }

    @Override
    public void setUp() {
        try {
            final Asset     home = getTestHome( search );
            Maybe<Asset> maybeRoot = search.getAssetAtPath(
                    pathFactory.createPath(home.getId(), "TreeToolTester" )
                    );
            if ( ! maybeRoot.isSet() ) {
                maybeRoot = Maybe.something(
                        manager.saveAsset(
                            AssetType.GENERIC.create().name( "TreeToolTester" ).parent( home).build(),
                            "Setup test"
                            )
                        );
            }
            final Asset aParent = maybeRoot.get();
            final Map<String,UUID> mapChildren = search.getAssetIdsFrom( aParent.getId(), null );
            for ( int i=0; i < 3; ++i ) {
                final String sChild = "Child" + i;
                final Maybe<UUID> maybeChildId = Maybe.emptyIfNull(mapChildren.get( sChild ));
                final Asset aChild;
                final Map<String,UUID> mapBrat;
                if ( ! maybeChildId.isSet() ) {
                    aChild = manager.saveAsset( AssetType.GENERIC.create().name( sChild ).parent( aParent).build(), "Setup test");
                    mapBrat = Collections.emptyMap();
                } else {
                    aChild = search.getAsset( maybeChildId.get() ).get();
                    mapBrat = search.getAssetIdsFrom( aChild.getId(), null );
                }
                for( int j=0; j <= i; ++j ) {
                    final String sBrat = "Brat" + j;
                    if ( ! mapBrat.containsKey( sBrat ) ) {
                        manager.saveAsset( AssetType.GENERIC.create().name(sBrat).parent( aChild).build(),
                                "Setup test"
                                );
                    }
                }
            }
        } catch (Exception ex) {
            olog.log(Level.WARNING, "Setup failed", ex);
            fail("Setup failed");
        }
    }

    /**
     * Just load a test tree
     */
    public void testTreeTool() {
        try {
            final List<Asset> vTree = treeTool.loadBreadthFirst(
                    search.getAssetAtPath(
                        pathFactory.createPath( "/" + getTestHome() + "/" + "TreeToolTester" )
                        ).get().getId()
                    );
            assertTrue( "Tree has expected size (10): " + vTree.size(),
                    10 == vTree.size()
                    );
        } catch ( Exception ex ) {
            olog.log( Level.WARNING, "Test caught exception", ex );
            fail( "Test failed" );
        }
    }
}
