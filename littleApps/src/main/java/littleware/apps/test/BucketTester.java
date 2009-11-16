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
import littleware.asset.*;
import littleware.apps.filebucket.*;
import littleware.base.*;
import littleware.test.LittleTest;


/**
 * Tester for the file-bucket tools in the
 * littleware.apps.filebucket package.
 */
public class BucketTester extends LittleTest {

    private static final String os_test_data = "Frickjack bla bla " + (new Date()).getTime();
    private static final String os_test_folder = "BucketTester";
    private static final Logger log = Logger.getLogger("littleware.apps.test.BucketTester");
    private final BucketManager bucketMgr;
    private final AssetManager assetMgr;
    private final AssetSearchManager search;
    private Asset oa_test = null;

    /**
     * Stash managers to use during test
     */
    @Inject
    public BucketTester(AssetManager m_asset, AssetSearchManager m_search, BucketManager m_bucket) {
        assetMgr = m_asset;
        search = m_search;
        bucketMgr = m_bucket;
        setName("testBucket");
    }

    
    /**
     * Setup a test asset.
     */
    @Override
    public void setUp() {
        try {
            final Asset home = getTestHome( search );
            UUID u_test_folder = search.getAssetIdsFrom(home.getId(), AssetType.GENERIC).get(os_test_folder);
            if (null == u_test_folder) {
                u_test_folder = assetMgr.saveAsset(
                        AssetType.GENERIC.create().parent(home).
                            name(os_test_folder).build(),
                             "setup folder for test"
                             ).getId();
            }

            final Date t_now = new Date();
            

            oa_test = assetMgr.saveAsset(
                    AssetType.GENERIC.create().parent( search.getAsset(u_test_folder).get()).
                                name("test" + t_now.getTime()).build()
                                , "setup new test"
                                );
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed setup, caught: " + e + ", " + BaseException.getStackTrace(e));
            throw new AssertionFailedException("Failed setup, caught: " + e, e);
        }
    }

    /**
     * Delete the test asset
     */
    @Override
    public void tearDown() {
        try {
            assetMgr.deleteAsset(oa_test.getId(), "Cleanup after test");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new AssertionFailedException("Failed setup, caught: " + e, e);
        }
    }

    /**
     * Try to write some data to a bucket under a test asset.
     */
    public void testBucket() {
        try {
            Bucket bucket_test = bucketMgr.getBucket(oa_test.getId());
            assertTrue("Bucket tracks right asset", bucket_test.getAssetId().equals(oa_test.getId()));
            assertTrue("Bucket starts empty", bucket_test.getPaths().isEmpty());
            oa_test = bucketMgr.writeToBucket(oa_test, "test1", os_test_data, "writing test data");
            bucket_test = bucketMgr.getBucket(oa_test.getId());

            assertTrue("Bucket has a path now", bucket_test.getPaths().contains("test1"));
            oa_test = bucketMgr.renameFile(oa_test, "test1", "test_rename", "bucket rename test");
            bucket_test = bucketMgr.getBucket(oa_test.getId());

            assertTrue("Rename went ok", bucket_test.getPaths().contains("test_rename") && (bucket_test.getPaths().size() == 1));
            oa_test = bucketMgr.writeToBucket(oa_test, "test1", os_test_data + os_test_data, "write another test file to the bucket");
            bucket_test = bucketMgr.getBucket(oa_test.getId());

            assertTrue("Bucket with multiple files went ok", bucket_test.getPaths().contains("test_rename") && bucket_test.getPaths().contains("test1") && (bucket_test.getPaths().size() == 2));

            String s_data = bucketMgr.readTextFromBucket(oa_test.getId(), "test_rename");
            assertTrue("Got expected data from bucket: " + s_data, s_data.equals(os_test_data));
            oa_test = bucketMgr.eraseFromBucket(oa_test, "test_rename", "erase some test data");
            bucket_test = bucketMgr.getBucket(oa_test.getId());

            assertTrue("Erase 1 went ok", bucket_test.getPaths().contains("test1") && (bucket_test.getPaths().size() == 1));

            oa_test = bucketMgr.eraseFromBucket(oa_test, "test1", "erase test1 from bucket");
            bucket_test = bucketMgr.getBucket(oa_test.getId());

            assertTrue("Erase to empty ok", bucket_test.getPaths().isEmpty());
        } catch (Exception e) {
            log.log(Level.WARNING, "Caught: " + e + ", " + BaseException.getStackTrace(e));
            assertTrue("Caught: " + e, false);
        }
    }
}

