package littleware.security.server.internal;

import com.google.common.collect.ImmutableMap;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.*;
import littleware.asset.server.LittleContext;
import littleware.asset.server.LittleTransaction;
import littleware.asset.server.ServerAssetManager;
import littleware.asset.server.ServerSearchManager;
import littleware.security.server.QuotaUtil;
import littleware.base.*;
import littleware.security.*;

/**
 * Factored out implementation  of quota management.
 */
public class SimpleQuotaUtil implements QuotaUtil {

    private static final Logger log = Logger.getLogger(SimpleQuotaUtil.class.getName());

    @Override
    public Quota getQuota(LittleContext ctx, LittleUser user, ServerSearchManager search) throws BaseException,
            GeneralSecurityException {
        final ImmutableMap<String, AssetInfo> v_quotas = search.getAssetIdsFrom(ctx, user.getId(),
                Quota.QUOTA_TYPE);
        final AssetInfo childInfo = v_quotas.get("littleware_quota");
        if (null == childInfo) {
            return null;
        }

        return (Quota) search.getAsset(ctx, childInfo.getId()).orElse(null);
    }

    @Override
    public int incrementQuotaCount(LittleContext ctx, final LittleUser user,
            final ServerAssetManager assetMgr,
            final ServerSearchManager search) throws BaseException, AssetException,
            GeneralSecurityException {

        int i_ops_left = -1;  // quota ops left
        final List<Quota.Builder> v_chain = new ArrayList<>();
        final Date now = new Date();
        final LittleTransaction trans = ctx.getTransaction();
        trans.startDbAccess();
        try {
            for (Quota quota = getQuota(ctx, user, search);
                    null != quota;
                    quota = (null != quota.getNextInChainId()) ? ((Quota) search.getAsset(ctx, quota.getNextInChainId(), -1L).getAsset().orElse(null))
                            : null) {
                final Quota.Builder quotaBuilder = quota.copy().narrow();
                v_chain.add(quotaBuilder);
                if ((null != quota.getEndDate()) && (quota.getEndDate().getTime() < now.getTime())) {
                    long l_period = quota.getEndDate().getTime()
                            - quota.getStartDate().getTime();

                    if (l_period < 1000000) {
                        l_period = 1000000;
                    }
                    Date t_end = new Date(l_period + now.getTime());
                    quotaBuilder.setStartDate(now);
                    quotaBuilder.setEndDate(t_end);
                    quotaBuilder.setQuotaCount(0);
                } else if ((quota.getQuotaLimit() >= 0) && (quota.getQuotaLimit() < quota.getQuotaCount())) {
                    throw new QuotaException("Quota exceeded: " + quota.getQuotaLimit()
                            + " less than " + quota.getQuotaCount());
                }

                int i_left = quotaBuilder.getQuotaLimit()
                        - quotaBuilder.getQuotaCount();

                if ((i_left >= 0) && (i_left < i_ops_left)) {
                    i_ops_left = i_left;
                }
            }

            for (Quota.Builder quotaBuilder : v_chain) {
                quotaBuilder.incrementQuotaCount();
                // don't worry about missed transactions
                quotaBuilder.setTimestamp(0L);

                log.log(Level.FINE, "Incrementing quota count on {0}", quotaBuilder.getId());
                assetMgr.saveAsset(ctx, quotaBuilder.build(), "update quota count");
            }
            return Integer.valueOf(i_ops_left);
        } finally {
            trans.endDbAccess();
        }
    }
}
