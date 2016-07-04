/*
 * Copyright 2011 http://code.google.com/p/littleware/
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.client;

import littleware.asset.client.AssetSearchManager;
import littleware.asset.client.AssetManager;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import littleware.asset.*;
import littleware.asset.LinkAsset.LinkBuilder;
import littleware.asset.TreeNode.TreeNodeBuilder;
import littleware.asset.client.test.AbstractAssetTest;

/**
 * Test traversal of some asset paths.
 */
public class AssetPathTester extends AbstractAssetTest {

    private static final Logger log = Logger.getLogger(AssetPathTester.class.getName());
    private final AssetManager assetMgr;
    private final AssetSearchManager search;
    private final AssetPathFactory pathFactory;
    private final Provider<TreeNodeBuilder> nodeProvider;
    private final Provider<LinkBuilder> linkProvider;

    /**
     * Stash AssetSearchManager instance to run tests against
     *
     * @param s_test_name of test to run - pass to super class
     * @param search to test against
     * @param assetMgr to setup test assets with if necessary
     */
    @Inject
    public AssetPathTester(AssetSearchManager search,
            AssetManager assetMgr,
            AssetPathFactory pathFactory,
            Provider<TreeNode.TreeNodeBuilder> nodeProvider,
            Provider<LinkAsset.LinkBuilder>  linkProvider
            ) {
        this.search = search;
        this.assetMgr = assetMgr;
        this.pathFactory = pathFactory;
        this.nodeProvider = nodeProvider;
        this.linkProvider = linkProvider;
        setName("testPathTraverse");
    }

    /**
     * Setup a test asset tree under the littleware.test_home:
     *         littleware.test_home/AssetPathTester/A,Points2A/1,2,3,biggest,smallest
     *
     * @TODO - switch over to mock-based test
     */
    @Override
    public void setUp() {
        try {
            final LittleHome testHome = getTestHome(search);
            TreeNode testFolder = (TreeNode) search.getAssetFrom(testHome.getId(), "AssetPathTester").getOr(null);
            if (null == testFolder) {
                testFolder = assetMgr.saveAsset(
                        nodeProvider.get().parent(testHome).
                        name("AssetPathTester").
                        comment("AssetPath traversal test area").build(), 
                        "Setting up AssetPathTester test area"
                        );
            }
            TreeNode nodeA = (TreeNode) search.getAssetFrom(testFolder.getId(), "A").getOr(null);
            LinkAsset pointer = (LinkAsset) search.getAssetFrom(testFolder.getId(), "Points2A").getOr(null);
            if (null == nodeA) {
                nodeA = assetMgr.saveAsset(
                        nodeProvider.get().parent(testFolder).
                        name("A").
                        comment("AssetPath traversal test area").build(), 
                        "Setting up AssetPathTester test area"
                        );

                if (null != pointer) {
                    pointer = assetMgr.saveAsset(pointer.copy().narrow( LinkAsset.LinkBuilder.class ).toId(nodeA.getId()).build(), "Update TO pointer for new A asset");
                }
            }
            if (null == pointer) {
                pointer = assetMgr.saveAsset(
                        linkProvider.get().
                        name("Points2A").
                        comment("link to A").
                        from(testFolder).
                        toId(nodeA.getId()).build(), "Setup LINK in AssetPathTester test tree");
            }
            final TreeNode[] numberNodes = new TreeNode[3];
            for (int i = 1; i < 4; ++i) {
                String numberName = Integer.toString(i);
                TreeNode numberNode = (TreeNode) search.getAssetFrom(nodeA.getId(), numberName).getOr(null);
                if (null == numberNode) {
                    numberNode = assetMgr.saveAsset(
                            nodeProvider.get().
                            name(numberName).
                            comment("Setting up AssetPathTester").
                            parent(nodeA).
                            build(), "Setting up AssetPathTester");
                }
                numberNodes[i - 1] = numberNode;
            }
            LinkAsset smallestPointer = (LinkAsset) search.getAssetFrom(nodeA.getId(), "smallest").getOr(null);
            if (null == smallestPointer) {
                smallestPointer = assetMgr.saveAsset(
                        linkProvider.get().
                        name("smallest").
                        from(nodeA).
                        toId(numberNodes[0].getId()).
                        comment("link to smallest number").build(), "Setting up AssetPathTester");
            }
            LinkAsset bigestPointer = (LinkAsset) search.getAssetFrom(nodeA.getId(), "biggest").getOr(null);
            if (null == bigestPointer) {
                bigestPointer = assetMgr.saveAsset(
                        linkProvider.get().
                        name("biggest").
                        from(nodeA).
                        toId(numberNodes[numberNodes.length - 1].getId()).
                        comment("link to biggest number").build(), "Setting up AssetPathTester");
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to setup AssetPathTester", e );
            fail("Failed setup, caught: " + e );
        }
    }


    /**
     * Traverse some test assets under 
     *             /byname:littleware.test_home:type:littleware.HOME/AssetPathTester/bla
     */
    public void testPathTraverse() {
        final List<AssetPath> testPaths = new ArrayList<AssetPath>();

        try {
            testPaths.add(pathFactory.createPath(getTestHome() + "/AssetPathTester/A/../A/1/../1"));
            testPaths.add(pathFactory.createPath("/" + getTestHome() + "/AssetPathTester/A/2/../../Points2A/2/"));
            testPaths.add(pathFactory.createPath(getTestHome() + "/AssetPathTester/Points2A/3"));

            assertTrue("Got a search manager", null != search);
            int i_count = 0;
            final LittleHome testHome = getTestHome( search );
            for (AssetPath path : testPaths) {
                ++i_count;
                log.log( Level.INFO, "Testing normalized path: " + path );
                assertTrue("Path properly normalized: " + path,
                        path.toString().indexOf("..") < 0);
                assertTrue("Path has expected basename " + i_count + ": " + path.getBasename(),
                        path.getBasename().equals(Integer.toString(i_count)));
                final AssetPath rootPath = pathFactory.createPath( testHome.getId(),
                        path.getSubRootPath());
                assertTrue("SubRoot paths match: " + path.getSubRootPath() + " == " +
                        rootPath.getSubRootPath(),
                        path.getSubRootPath().equals(rootPath.getSubRootPath()));
                final AssetPath path_name_string = pathFactory.createPath(path.toString());
                final AssetPath path_id_string = pathFactory.createPath( testHome.getId(),
                        rootPath.getSubRootPath());

                assertTrue(path_name_string.toString() + " == " + path.toString(),
                        path_name_string.equals(path));
                assertTrue(path_id_string.toString() + " == " + rootPath.toString(),
                        path_id_string.equals(rootPath));
                final Asset assetAtPath = search.getAssetAtPath( path ).get();
                assertTrue("Got the same asset from " + path + " and " + rootPath,
                        assetAtPath.equals( search.getAssetAtPath( rootPath ).get() ));
                final AssetPath rootedPath = search.toRootedPath(pathFactory.createPath(assetAtPath.getId()));
                assertTrue("Root path resolver works ok: " + rootedPath,
                        search.getAssetAtPath(rootedPath).get().equals(assetAtPath)
                        && rootedPath.toString().equals(search.toRootedPath(pathFactory.createPath(assetAtPath.getId())).toString())
                        );
                final AssetPath path_parent = path.getParent();
                assertTrue("Path has parent: " + path,
                        path.hasParent());
                assertTrue("Path compares greater than parent: " + path + " <>? " + path_parent,
                        path.compareTo(path_parent) > 0);
                //---
                assertTrue("Got expected name: " + i_count + " == " + assetAtPath.getName(),
                        Integer.toString(i_count).equals(assetAtPath.getName()));

                if (1 == i_count) {
                    final AssetPath path2Smallest = pathFactory.createPath(getTestHome() + "/AssetPathTester/A//smallest");
                    final Asset smallest = search.getAssetAtPath( path2Smallest).get();
                    assertTrue("Smallest link resolved to asset 1: " + smallest.getName(),
                            smallest.equals(assetAtPath));
                    assertTrue("Get by id ok",
                            search.getAssetAtPath( pathFactory.createPath(smallest.getId().toString()) ).get().equals(smallest));
                } else if (i_count == testPaths.size()) {
                    final AssetPath path2Biggest = pathFactory.createPath(getTestHome() + "/AssetPathTester/A/biggest");
                    final Asset biggest = search.getAssetAtPath( path2Biggest ).get();
                    assertTrue("Biggest link resolved to asset 1: " + biggest.getName(),
                            biggest.equals(assetAtPath));
                }
            }

        } catch (Exception ex) {
            log.log(Level.WARNING, "Failed test", ex );
            fail("Caught: " + ex);
        }
    }

    /**
     * Check that path-lookup fails on path that does not exist
     */
    public void testBadLookup() {
        try {
            final AssetPath path = pathFactory.createPath("/" + getTestHome() + "/bogusFrickjack/bogus/ugh");
            assertTrue("Path not found: " + path, search.getAssetAtPath(path).isEmpty());
        } catch (Exception ex) {
            log.log(Level.WARNING, "Test failed", ex );
            fail("Caught: " + ex );
        }
    }
}
