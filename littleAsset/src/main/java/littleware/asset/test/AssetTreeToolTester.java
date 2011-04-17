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
import com.google.inject.Provider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.Asset;
import littleware.asset.AssetManager;
import littleware.asset.AssetPath;
import littleware.asset.AssetPathFactory;
import littleware.asset.AssetSearchManager;
import littleware.asset.AssetTreeTemplate;
import littleware.asset.AssetTreeTemplate.TemplateBuilder;
import littleware.asset.AssetTreeTool;
import littleware.asset.AssetType;
import littleware.base.Maybe;

/**
 * Simple AssetTreeTool tester
 */
public class AssetTreeToolTester extends AbstractAssetTest {

    private static final Logger log = Logger.getLogger(AssetTreeTool.class.getName());
    private final AssetTreeTool treeTool;
    private final AssetManager manager;
    private final AssetPathFactory pathFactory;
    private final AssetSearchManager search;
    private final Provider<TemplateBuilder> treeBuilder;

    @Inject
    public AssetTreeToolTester(AssetManager manager,
            AssetTreeTool treeTool,
            AssetPathFactory pathFactory,
            AssetSearchManager search,
            Provider<AssetTreeTemplate.TemplateBuilder> treeBuilder) {
        this.treeTool = treeTool;
        this.search = search;
        this.manager = manager;
        this.pathFactory = pathFactory;
        this.treeBuilder = treeBuilder;
        setName("testTreeTool");
    }

    @Override
    public void setUp() {
        try {
            final Asset home = getTestHome(search);
            Maybe<Asset> maybeRoot = search.getAssetAtPath(
                    pathFactory.createPath(home.getId(), "TreeToolTester"));
            if (!maybeRoot.isSet()) {
                maybeRoot = Maybe.something(
                        manager.saveAsset(
                        GenericAsset.GENERIC.create().name("TreeToolTester").parent(home).build(),
                        "Setup test"));
            }
            final Asset aParent = maybeRoot.get();
            final Map<String, UUID> mapChildren = search.getAssetIdsFrom(aParent.getId(), null);
            for (int i = 0; i < 3; ++i) {
                final String sChild = "Child" + i;
                final Maybe<UUID> maybeChildId = Maybe.emptyIfNull(mapChildren.get(sChild));
                final Asset aChild;
                final Map<String, UUID> mapBrat;
                if (!maybeChildId.isSet()) {
                    aChild = manager.saveAsset(GenericAsset.GENERIC.create().name(sChild).parent(aParent).build(), "Setup test");
                    mapBrat = Collections.emptyMap();
                } else {
                    aChild = search.getAsset(maybeChildId.get()).get();
                    mapBrat = search.getAssetIdsFrom(aChild.getId(), null);
                }
                for (int j = 0; j <= i; ++j) {
                    final String sBrat = "Brat" + j;
                    if (!mapBrat.containsKey(sBrat)) {
                        manager.saveAsset(GenericAsset.GENERIC.create().name(sBrat).parent(aChild).build(),
                                "Setup test");
                    }
                }
            }
        } catch (Exception ex) {
            log.log(Level.WARNING, "Setup failed", ex);
            fail("Setup failed");
        }
    }

    /**
     * Just load a test tree
     */
    public void testTreeTool() {
        try {
            final AssetPath testFolderPath = pathFactory.createPath("/" + getTestHome() + "/" + "TreeToolTester");
            final List<Asset> vTree = treeTool.loadBreadthFirst(
                    search.getAssetAtPath(
                    testFolderPath).get().getId());
            assertTrue("Tree has expected size (10): " + vTree.size(),
                    10 == vTree.size());

            final List<AssetTreeTemplate> children = new ArrayList<AssetTreeTemplate>();
            for (int i = 0; i < 3; ++i) {
                children.add(
                        treeBuilder.get().assetBuilder(
                        GenericAsset.GENERIC.create().name("Child" + i)).build());
            }
            final Collection<AssetTreeTemplate.AssetInfo> infoList =
                    treeBuilder.get().path(testFolderPath).addChildren(
                    children).build().visit(search);
            assertTrue( "Tree template visits 5 nodes: " + infoList.size(),
                    5 == infoList.size()
                    );
            for( AssetTreeTemplate.AssetInfo info : infoList ) {
                assertTrue( "Template visit finds already existing nodes: " + info.getAsset().getName(),
                        info.getAssetExists()
                        );
            }
        } catch (Exception ex) {
            log.log(Level.WARNING, "Test caught exception", ex);
            fail("Test failed");
        }
    }
}
