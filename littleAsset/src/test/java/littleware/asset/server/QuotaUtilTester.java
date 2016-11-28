package littleware.asset.server;

import com.google.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.server.LittleContext.ContextFactory;
import littleware.security.LittleUser;
import littleware.security.Quota;
import littleware.security.auth.LittleSession;
import littleware.security.server.QuotaUtil;
import littleware.test.LittleTestRunner;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test the goofy QuotaUtil
 */
@RunWith(LittleTestRunner.class)
public class QuotaUtilTester extends ServerTestBase {
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
    }

    /**
     * Just verify that incrementQuota increments the quota asset.
     * Must be running the test as a user with an active Quota set.
     */
    @Test
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
