/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.asset.server.test;

import com.google.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.client.AssetManager;
import littleware.asset.client.AssetSearchManager;
import littleware.asset.test.AbstractAssetTest;
import littleware.security.LittleUser;
import littleware.security.Quota;
import littleware.security.server.QuotaUtil;

/**
 * Test the goofy QuotaUtil
 */
public class QuotaUtilTester extends AbstractAssetTest {
    private static final Logger log = Logger.getLogger( QuotaUtilTester.class.getName() );
    private final QuotaUtil quotaUtil;
    private final AssetManager assetMgr;
    private final AssetSearchManager search;
    private final LittleUser caller;

    @Inject
    public QuotaUtilTester( QuotaUtil quotaUtil,
            AssetManager assetMgr,
            AssetSearchManager search,
            LittleUser caller
            ) {
        this.quotaUtil = quotaUtil;
        this.assetMgr = assetMgr;
        this.search = search;
        this.caller = caller;
        setName( "testQuota" );
    }

    /**
     * Just verify that incrementQuota increments the quota asset.
     * Must be running the test as a user with an active Quota set.
     */
    public void testQuota() {
        try {
            final Quota quotaBefore = quotaUtil.getQuota(caller, search);
            assertTrue("Got a quota we can test against",
                    (null != quotaBefore) && (quotaBefore.getQuotaLimit() > 0) && (quotaBefore.getQuotaCount() >= 0));
            quotaUtil.incrementQuotaCount( caller, assetMgr, search );
            final Quota a_quota_after = quotaUtil.getQuota(caller,search);
            assertTrue("Quota incremented by 1: " + quotaBefore.getQuotaCount() +
                    " -> " + a_quota_after.getQuotaCount(),
                    quotaBefore.getQuotaCount() + 1 == a_quota_after.getQuotaCount());
            // Verify get/setData parsing
            assertTrue("get/setData consistency",
                    a_quota_after.getQuotaLimit() == a_quota_after.copy().build().getQuotaLimit()
                    );
        } catch (Exception ex) {
            log.log(Level.WARNING, "Failed test", ex );
            fail("Caught exception: " + ex );
        }
    }

}
