/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.test;

import com.google.inject.Inject;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import littleware.apps.filebucket.Bucket;
import littleware.apps.filebucket.BucketManager;
import littleware.apps.filebucket.BucketUtil;
import littleware.asset.Asset;
import littleware.asset.AssetManager;
import littleware.asset.AssetSearchManager;
import littleware.asset.AssetType;
import littleware.base.BaseException;
import littleware.base.feedback.Feedback;
import littleware.test.LittleTest;


/**
 * Tester for the file-bucket tools in the
 * littleware.apps.filebucket package.
 */
public class BucketTester extends LittleTest {

    private static final String testData = "Frickjack bla bla " + (new Date()).getTime();
    private static final String testFolderName = "BucketTester";
    private static final Logger log = Logger.getLogger(BucketTester.class.getName() );
    private final BucketManager bucketMgr;
    private final AssetManager assetMgr;
    private final AssetSearchManager search;
    private Asset testAsset = null;
    private final BucketUtil bucketUtil;
    private final Feedback feedback;

    /**
     * Stash managers to use during test
     */
    @Inject
    public BucketTester(AssetManager assetMgr, AssetSearchManager seach,
            BucketManager bucketMgr,
            BucketUtil bucketUtil,
            Feedback   feedback
            ) {
        this.assetMgr = assetMgr;
        this.search = seach;
        this.bucketMgr = bucketMgr;
        this.bucketUtil = bucketUtil;
        this.feedback=feedback;
        setName("testBucket");
    }

    
    /**
     * Setup a test asset.
     */
    @Override
    public void setUp() {
        try {
            final Asset home = getTestHome( search );
            UUID testFolderId = search.getAssetIdsFrom(home.getId(), AssetType.GENERIC).get(testFolderName);
            if (null == testFolderId) {
                testFolderId = assetMgr.saveAsset(
                        AssetType.GENERIC.create().parent(home).
                            name(testFolderName).build(),
                             "setup folder for test"
                             ).getId();
            }

            final Date now = new Date();
            
            testAsset = assetMgr.saveAsset(
                    AssetType.GENERIC.create().parent( search.getAsset(testFolderId).get()).
                                name("test" + now.getTime()).build()
                                , "setup new test"
                                );
        } catch (Exception ex) {
            log.log(Level.WARNING, "Failed setup", ex );
            fail( "Caught exception: " + ex );
        }
    }

    /**
     * Delete the test asset
     */
    @Override
    public void tearDown() {
        try {
            assetMgr.deleteAsset(testAsset.getId(), "Cleanup after test");
        } catch (Exception ex) {
            log.log( Level.WARNING, "Failed teardown", ex );
            fail( "Caught exception: " + ex );
        }
    }

    /**
     * Try to write some data to a bucket under a test asset.
     */
    public void testBucket() {
        try {
            Bucket bucket = bucketMgr.getBucket(testAsset.getId());
            assertTrue("Bucket tracks right asset", bucket.getAssetId().equals(testAsset.getId()));
            assertTrue("Bucket starts empty", bucket.getPaths().isEmpty());
            testAsset = bucketUtil.writeText(testAsset, "test1", testData, "writing test data", feedback );
            bucket = bucketMgr.getBucket(testAsset.getId());

            assertTrue("Bucket has a path now", bucket.getPaths().contains("test1"));
            testAsset = bucketMgr.renameFile(testAsset, "test1", "test_rename", "bucket rename test");
            bucket = bucketMgr.getBucket(testAsset.getId());

            assertTrue("Rename went ok", bucket.getPaths().contains("test_rename") && (bucket.getPaths().size() == 1));
            testAsset = bucketUtil.writeText(testAsset, "test1", testData + testData,
                                    "write another test file to the bucket", feedback
                                    );
            bucket = bucketMgr.getBucket(testAsset.getId());

            assertTrue("Bucket with multiple files went ok", bucket.getPaths().contains("test_rename") && bucket.getPaths().contains("test1") && (bucket.getPaths().size() == 2));

            final String readData = bucketUtil.readText(testAsset.getId(), "test_rename", feedback );
            assertTrue("Got expected data from bucket: " + readData, readData.equals(testData));
            testAsset = bucketMgr.eraseFromBucket(testAsset, "test_rename", "erase some test data");
            bucket = bucketMgr.getBucket(testAsset.getId());

            assertTrue("Erase 1 went ok", bucket.getPaths().contains("test1") && (bucket.getPaths().size() == 1));

            testAsset = bucketMgr.eraseFromBucket(testAsset, "test1", "erase test1 from bucket");
            bucket = bucketMgr.getBucket(testAsset.getId());

            assertTrue("Erase to empty ok", bucket.getPaths().isEmpty());
        } catch (Exception ex) {
            log.log(Level.WARNING, "Caught exception", ex );
            fail("Caught: " + ex );
        }
    }
}

