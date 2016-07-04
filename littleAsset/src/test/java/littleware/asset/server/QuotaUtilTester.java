/*
 * Copyright 2011 http://code.google.com/p/littleware/
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.server;

import com.google.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.server.LittleContext.ContextFactory;
import littleware.asset.server.ServerAssetManager;
import littleware.asset.server.ServerSearchManager;
import littleware.security.LittleUser;
import littleware.security.Quota;
import littleware.security.auth.LittleSession;
import littleware.security.server.QuotaUtil;

/**
 * Test the goofy QuotaUtil
 */
public class QuotaUtilTester extends AbstractServerTest {
    private static final Logger log = Logger.getLogger( QuotaUtilTester.class.getName() );

    private final QuotaUtil           quotaUtil;
    private final ServerAssetManager  assetMgr;
    private final ServerSearchManager search;

    
    @Inject
    public QuotaUtilTester( QuotaUtil quotaUtil,
            ServerAssetManager assetMgr,
            ServerSearchManager search,
            LittleSession session,
            ContextFactory ctxFactory
            ) {
        super(session, ctxFactory);
        this.quotaUtil = quotaUtil;
        this.assetMgr = assetMgr;
        this.search = search;
        setName( "testQuota" );
    }

    /**
     * Just verify that incrementQuota increments the quota asset.
     * Must be running the test as a user with an active Quota set.
     */
    public void testQuota() {
        final LittleUser    caller = getContext().getCaller();
        try {
            final Quota quotaBefore = quotaUtil.getQuota(getContext(), caller, search);
            assertTrue("Got a quota we can test against",
                    (null != quotaBefore) && (quotaBefore.getQuotaLimit() > 0) && (quotaBefore.getQuotaCount() >= 0));
            quotaUtil.incrementQuotaCount( getContext().getAdminContext(), caller, assetMgr, search );
            final Quota quotaAfter = quotaUtil.getQuota(getContext(), caller,search);
            assertTrue("Quota incremented by 1: " + quotaBefore.getQuotaCount() +
                    " -> " + quotaAfter.getQuotaCount(),
                    quotaBefore.getQuotaCount() + 1 == quotaAfter.getQuotaCount());
            // Verify get/setData parsing
            assertTrue("get/setData consistency",
                    quotaAfter.getQuotaLimit() == quotaAfter.copy().build().narrow( Quota.class ).getQuotaLimit()
                    );
        } catch (Exception ex) {
            log.log(Level.WARNING, "Failed test", ex );
            fail("Caught exception: " + ex );
        } 
    }

}
