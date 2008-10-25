package littleware.apps.test;

import com.google.inject.Inject;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.mail.internet.*;
import java.net.*;
import junit.framework.*;
import littleware.asset.*;
import littleware.asset.xml.*;
import littleware.apps.filebucket.*;
import littleware.base.*;
import littleware.security.*;


/**
 * Tester for the file-bucket tools in the
 * littleware.apps.filebucket package.
 */
public class BucketTester extends TestCase {

    private static final String os_test_data = "Frickjack bla bla " + (new Date()).getTime();
    private static final String os_test_folder = "BucketTester";
    private static final Logger olog_generic = Logger.getLogger("littleware.apps.test.BucketTester");
    private final BucketManager om_bucket;
    private final AssetManager om_asset;
    private final AssetSearchManager om_search;
    private Asset oa_test = null;

    /**
     * Stash managers to use during test
     */
    public BucketTester(String s_test_name, AssetManager m_asset, AssetSearchManager m_search, BucketManager m_bucket) {
        super(s_test_name);
        om_asset = m_asset;
        om_search = m_search;
        om_bucket = m_bucket;
    }

    /**
     * Inject dependencies
     */
    @Inject
    public BucketTester( AssetManager m_asset, AssetSearchManager m_search, BucketManager m_bucket) {
        this( "", m_asset, m_search, m_bucket );
    }
    
    /**
     * Setup a test asset.
     */
    public void setUp() {
        try {
            UUID u_home = om_search.getHomeAssetIds().get( "littleware.test_home" );
            UUID u_test_folder = om_search.getAssetIdsFrom(u_home, AssetType.GENERIC).get(os_test_folder);
            if (null == u_test_folder) {
                Asset a_folder = AssetType.GENERIC.create();
                a_folder.setFromId(u_home);
                a_folder.setHomeId(u_home);
                a_folder.setName(os_test_folder);
                a_folder.setAclId(om_search.getByName(littleware.security.AclManager.ACL_EVERYBODY_READ, SecurityAssetType.ACL).getObjectId());
                a_folder = om_asset.saveAsset(a_folder, "setup folder for test");
                u_test_folder = a_folder.getObjectId();
            }

            Date t_now = new Date();
            Asset a_test = AssetType.GENERIC.create();
            a_test.setFromId(u_test_folder);
            a_test.setHomeId(u_home);
            a_test.setName("test" + t_now.getTime());
            oa_test = om_asset.saveAsset(a_test, "setup new test");
        } catch (Exception e) {
            olog_generic.log(Level.WARNING, "Failed setup, caught: " + e + ", " + BaseException.getStackTrace(e));
            throw new AssertionFailedException("Failed setup, caught: " + e, e);
        }
    }

    /**
     * Delete the test asset
     */
    public void tearDown() {
        try {
            om_asset.deleteAsset(oa_test.getObjectId(), "Cleanup after test");
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
            Bucket bucket_test = om_bucket.getBucket(oa_test.getObjectId());
            assertTrue("Bucket tracks right asset", bucket_test.getAssetId().equals(oa_test.getObjectId()));
            assertTrue("Bucket starts empty", bucket_test.getPaths().isEmpty());
            oa_test = om_bucket.writeToBucket(oa_test, "test1", os_test_data, "writing test data");
            bucket_test = om_bucket.getBucket(oa_test.getObjectId());

            assertTrue("Bucket has a path now", bucket_test.getPaths().contains("test1"));
            oa_test = om_bucket.renameFile(oa_test, "test1", "test_rename", "bucket rename test");
            bucket_test = om_bucket.getBucket(oa_test.getObjectId());

            assertTrue("Rename went ok", bucket_test.getPaths().contains("test_rename") && (bucket_test.getPaths().size() == 1));
            oa_test = om_bucket.writeToBucket(oa_test, "test1", os_test_data + os_test_data, "write another test file to the bucket");
            bucket_test = om_bucket.getBucket(oa_test.getObjectId());

            assertTrue("Bucket with multiple files went ok", bucket_test.getPaths().contains("test_rename") && bucket_test.getPaths().contains("test1") && (bucket_test.getPaths().size() == 2));

            String s_data = om_bucket.readTextFromBucket(oa_test.getObjectId(), "test_rename");
            assertTrue("Got expected data from bucket: " + s_data, s_data.equals(os_test_data));
            oa_test = om_bucket.eraseFromBucket(oa_test, "test_rename", "erase some test data");
            bucket_test = om_bucket.getBucket(oa_test.getObjectId());

            assertTrue("Erase 1 went ok", bucket_test.getPaths().contains("test1") && (bucket_test.getPaths().size() == 1));

            oa_test = om_bucket.eraseFromBucket(oa_test, "test1", "erase test1 from bucket");
            bucket_test = om_bucket.getBucket(oa_test.getObjectId());

            assertTrue("Erase to empty ok", bucket_test.getPaths().isEmpty());
        } catch (Exception e) {
            olog_generic.log(Level.WARNING, "Caught: " + e + ", " + BaseException.getStackTrace(e));
            assertTrue("Caught: " + e, false);
        }
    }
}
// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com