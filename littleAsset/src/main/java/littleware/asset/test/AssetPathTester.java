/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
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
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;


import littleware.asset.*;
import littleware.asset.LinkAsset.LinkBuilder;
import littleware.asset.TreeNode.TreeNodeBuilder;
import littleware.base.*;

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
     * @param m_search to test against
     * @param m_asset to setup test assets with if necessary
     */
    @Inject
    public AssetPathTester(AssetSearchManager m_search,
            AssetManager m_asset,
            AssetPathFactory pathFactory,
            Provider<TreeNode.TreeNodeBuilder> nodeProvider,
            Provider<LinkAsset.LinkBuilder>  linkProvider
            ) {
        search = m_search;
        assetMgr = m_asset;
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
                    pointer = assetMgr.saveAsset(pointer.copy().toId(nodeA.getId()).build(), "Update TO pointer for new A asset");
                }
            }
            if (null == pointer) {
                pointer = assetMgr.saveAsset(
                        linkProvider.get().
                        name("Points2A").
                        comment("link to A").
                        parent(testFolder).
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
                            toId(nodeA.getId()).build(), "Setting up AssetPathTester");
                }
                numberNodes[i - 1] = numberNode;
            }
            LinkAsset smallestPointer = (LinkAsset) search.getAssetFrom(nodeA.getId(), "smallest").getOr(null);
            if (null == smallestPointer) {
                smallestPointer = assetMgr.saveAsset(
                        linkProvider.get().
                        name("smallest").
                        parent(nodeA).
                        toId(numberNodes[0].getId()).
                        comment("link to smallest number").build(), "Setting up AssetPathTester");
            }
            LinkAsset bigestPointer = (LinkAsset) search.getAssetFrom(nodeA.getId(), "biggest").getOr(null);
            if (null == bigestPointer) {
                bigestPointer = assetMgr.saveAsset(
                        linkProvider.get().
                        name("biggest").
                        parent(nodeA).
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
     * No teardown necessary
     */
    @Override
    public void tearDown() {
    }

    /**
     * Traverse some test assets under 
     *             /byname:littleware.test_home:type:littleware.HOME/AssetPathTester/bla
     */
    public void testPathTraverse() {
        List<AssetPath> v_tests = new ArrayList<AssetPath>();

        try {
            v_tests.add(pathFactory.createPath("littleware.test_home/AssetPathTester/A/../A/1/../1"));
            v_tests.add(pathFactory.createPath("/littleware.test_home/AssetPathTester/A/2/../../Points2A/2/@/2"));
            v_tests.add(pathFactory.createPath("littleware.test_home/AssetPathTester/Points2A/2/@/3"));

            assertTrue("Got a search manager", null != search);
            int i_count = 0;
            for (AssetPath path_test : v_tests) {
                ++i_count;
                assertTrue("Path properly normalized: " + path_test,
                        path_test.toString().indexOf("..") < 0);
                assertTrue("Path has expected basename " + i_count + ": " + path_test.getBasename(),
                        path_test.getBasename().equals(Integer.toString(i_count)));
                AssetPath path_root = pathFactory.createPath(path_test.getRoot(search).get().getId(),
                        path_test.getSubRootPath());
                assertTrue("SubRoot paths match: " + path_test.getSubRootPath() + " == " +
                        path_root.getSubRootPath(),
                        path_test.getSubRootPath().equals(path_root.getSubRootPath()));
                AssetPath path_name_string = pathFactory.createPath(path_test.toString());
                AssetPath path_id_string = pathFactory.createPath(path_root.getRoot(search).get().getId(),
                        path_root.getSubRootPath());

                assertTrue(path_name_string.toString() + " == " + path_test.toString(),
                        path_name_string.equals(path_test));
                assertTrue(path_id_string.toString() + " == " + path_root.toString(),
                        path_id_string.equals(path_root));
                final Asset a_test = path_test.getAsset(search).get();
                assertTrue("Got the same asset from " + path_test + " and " + path_root,
                        a_test.equals(path_root.getAsset(search).get()));
                final AssetPath pathRoot = pathFactory.toRootedPath(pathFactory.createPath(a_test.getId()));
                assertTrue("Root path resolver works ok: " + pathRoot,
                        pathRoot.getAsset(search).equals(a_test) && pathRoot.toString().equals(pathFactory.toRootedPath(pathFactory.createPath(a_test.getId())).toString()));
                AssetPath path_parent = path_test.getParent();
                assertTrue("Path has parent: " + path_test,
                        path_test.hasParent());
                assertTrue("Path compares greater than parent: " + path_test + " <>? " + path_parent,
                        path_test.compareTo(path_parent) > 0);
                //---
                assertTrue("Got expected name: " + i_count + " == " + a_test.getName(),
                        Integer.toString(i_count).equals(a_test.getName()));

                if (1 == i_count) {
                    AssetPath path_smallest = pathFactory.createPath(path_test.toString() + "/@/smallest");
                    Asset a_smallest = path_smallest.getAsset(search).get();
                    assertTrue("Smallest link resolved to asset 1: " + a_smallest.getName(),
                            a_smallest.equals(a_test));
                    assertTrue("Get by id ok",
                            pathFactory.createPath(a_smallest.getId().toString()).getAsset(search).equals(a_smallest));
                } else if (i_count == v_tests.size()) {
                    AssetPath path_biggest = pathFactory.createPath(path_test.toString() + "/@/biggest");
                    Asset a_biggest = path_biggest.getAsset(search).get();
                    assertTrue("Biggest link resolved to asset 1: " + a_biggest.getName(),
                            a_biggest.equals(a_test));
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
