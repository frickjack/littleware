/*
 * Copyright 2011 http://code.google.com/p/littleware/
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.client.test;

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
import littleware.asset.client.AssetManager;
import littleware.asset.AssetPath;
import littleware.asset.AssetPathFactory;
import littleware.asset.client.AssetSearchManager;
import littleware.asset.AssetTreeTemplate;
import littleware.asset.AssetTreeTemplate.TemplateBuilder;
import littleware.asset.client.AssetTreeTool;
import littleware.asset.LittleHome;
import littleware.asset.TemplateScanner;
import littleware.asset.TemplateScanner.ExistInfo;
import littleware.asset.TreeNode;
import littleware.asset.TreeNode.TreeNodeBuilder;
import littleware.asset.client.AssetLibrary;
import littleware.asset.client.AssetRef;
import littleware.asset.client.ClientScannerFactory;
import littleware.base.Options;
import littleware.base.Option;

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
    private final Provider<TreeNodeBuilder> nodeProvider;
    private final Provider<TemplateScanner> treeScanFactory;
    private final AssetLibrary library;

    @Inject
    public AssetTreeToolTester(AssetManager manager,
            AssetTreeTool treeTool,
            AssetPathFactory pathFactory,
            AssetSearchManager search,
            Provider<TreeNode.TreeNodeBuilder> nodeProvider,
            Provider<AssetTreeTemplate.TemplateBuilder> treeBuilder,
            ClientScannerFactory treeScanFactory,
            AssetLibrary library) {
        this.treeTool = treeTool;
        this.search = search;
        this.manager = manager;
        this.pathFactory = pathFactory;
        this.treeBuilder = treeBuilder;
        this.nodeProvider = nodeProvider;
        setName("testTreeTool");
        this.treeScanFactory = treeScanFactory;
        this.library = library;
    }

    @Override
    public void setUp() {
        try {
            final LittleHome home = getTestHome(search);
            AssetRef maybeRoot = search.getAssetAtPath(
                    pathFactory.createPath(home.getId(), "TreeToolTester"));
            if (!maybeRoot.isSet()) {
                maybeRoot = library.syncAsset(
                        manager.saveAsset(
                        nodeProvider.get().name("TreeToolTester").parent(home).build(),
                        "Setup test"));
            }
            final TreeNode parentNode = maybeRoot.get().narrow();
            final Map<String, UUID> mapChildren = search.getAssetIdsFrom(parentNode.getId(), null);
            for (int i = 0; i < 3; ++i) {
                final String sChild = "Child" + i;
                final Option<UUID> maybeChildId = Options.some(mapChildren.get(sChild));
                final TreeNode childNode;
                final Map<String, UUID> mapBrat;
                if (!maybeChildId.isSet()) {
                    childNode = manager.saveAsset(nodeProvider.get().name(sChild).parent(parentNode).build(), "Setup test");
                    mapBrat = Collections.emptyMap();
                } else {
                    childNode = search.getAsset(maybeChildId.get()).get().narrow();
                    mapBrat = search.getAssetIdsFrom(childNode.getId(), null);
                }
                for (int j = 0; j <= i; ++j) {
                    final String sBrat = "Brat" + j;
                    if (!mapBrat.containsKey(sBrat)) {
                        manager.saveAsset(nodeProvider.get().name(sBrat).parent(childNode).build(),
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
                        nodeProvider.get().name("Child" + i)).build());
            }
            final Collection<AssetTreeTemplate.AssetInfo> infoList =
                    treeBuilder.get().path(testFolderPath).addChildren(
                    children).build().scan(getTestHome(search), treeScanFactory.get() );
            assertTrue("Tree template visits 4 nodes: " + infoList.size(),
                    4 == infoList.size());
            for (AssetTreeTemplate.AssetInfo x : infoList) {
                final ExistInfo info = (ExistInfo) x;
                assertTrue("Template visit finds already existing nodes: " + info.getAsset().getName(),
                        info.getAssetExists());
            }
        } catch (Exception ex) {
            log.log(Level.WARNING, "Test caught exception", ex);
            fail("Test failed");
        }
    }
}
