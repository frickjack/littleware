/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.server;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import littleware.asset.*;
import littleware.base.*;
import littleware.security.*;


/**
 * Factored out implementation  of quota management.
 */
public class SimpleQuotaUtil implements QuotaUtil {
    private static final Logger olog = Logger.getLogger( SimpleQuotaUtil.class.getName() );

    /** Cache the admin subject */
    private Subject oj_admin;
    private final Provider<LittleTransaction>   oprovideTrans;
    
    @Inject
    public SimpleQuotaUtil( Provider<LittleTransaction> provideTrans ) {
        oprovideTrans = provideTrans;
    }

    /**
     * Get a Subject representing the littleware admin
     */
    private Subject getAdmin( AssetSearchManager m_search ) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        if (null == oj_admin) {
            try {
                final LittleUser admin = m_search.getByName(AccountManager.LITTLEWARE_ADMIN, SecurityAssetType.USER).get().narrow();
                Set<Principal> v_users = new HashSet<Principal>();

                v_users.add(admin);
                oj_admin = new Subject(true, v_users, new HashSet<Object>(), new HashSet<Object>());
            } catch ( Exception ex ) {
                throw new AssertionFailedException("LITTLEWARE_ADMIN should exist, caught: " + ex, ex);
            } 
        }
        return oj_admin;
    }

    @Override
    public Quota getQuota(LittleUser p_user, AssetSearchManager m_search ) throws BaseException,
            GeneralSecurityException, RemoteException {
        Map<String, UUID> v_quotas = m_search.getAssetIdsFrom(p_user.getId(),
                SecurityAssetType.QUOTA);
        UUID u_child = v_quotas.get("littleware_quota");
        if (null == u_child) {
            return null;
        }

        return (Quota) m_search.getAsset(u_child).getOr( null );
    }


    @Override
    public int incrementQuotaCount( final LittleUser p_user,
            final AssetManager m_asset,
            final AssetSearchManager m_search
            ) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {

        try {
            return Subject.doAs(getAdmin(m_search),
                    new PrivilegedExceptionAction<Integer>() {

                @Override
                        public Integer run() throws Exception {
                            int i_ops_left = -1;  // quota ops left
                            final List<Quota.Builder> v_chain = new ArrayList<Quota.Builder>();
                            final LittleTransaction trans_quota = oprovideTrans.get();
                            Date t_now = new Date();

                            trans_quota.startDbAccess();
                            try {
                                for (Quota a_quota = getQuota(p_user, m_search);
                                        null != a_quota;
                                        a_quota = (null != a_quota.getToId()) ? ((Quota) m_search.getAsset(a_quota.getToId()).getOr(null))
                                                : null) {
                                    final Quota.Builder quotaBuilder = a_quota.copy();
                                    v_chain.add( quotaBuilder );
                                    if ((null != a_quota.getEndDate()) && (a_quota.getEndDate().getTime() < t_now.getTime())) {
                                        long l_period = a_quota.getEndDate().getTime() -
                                                a_quota.getStartDate().getTime();

                                        if (l_period < 1000000) {
                                            l_period = 1000000;
                                        }
                                        Date t_end = new Date(l_period + t_now.getTime());
                                        quotaBuilder.setStartDate(t_now);
                                        quotaBuilder.setEndDate(t_end);
                                        quotaBuilder.setQuotaCount(0);
                                    } else if ((a_quota.getQuotaLimit() >= 0) && (a_quota.getQuotaLimit() < a_quota.getQuotaCount())) {
                                        throw new QuotaException("Quota exceeded: " + a_quota.getQuotaLimit() +
                                                " less than " + a_quota.getQuotaCount());
                                    }

                                    int i_left = quotaBuilder.getQuotaLimit() -
                                            quotaBuilder.getQuotaCount();

                                    if ((i_left >= 0) && (i_left < i_ops_left)) {
                                        i_ops_left = i_left;
                                    }
                                }

                                for (Quota.Builder quotaBuilder : v_chain) {
                                    quotaBuilder.incrementQuotaCount();
                                    // don't worry about missed transactions
                                    quotaBuilder.setTransaction(0L);

                                    olog.log(Level.FINE, "Incrementing quota count on " + quotaBuilder.getId());
                                    m_asset.saveAsset(quotaBuilder.build(), "update quota count");
                                }
                                return Integer.valueOf(i_ops_left);
                            } finally {
                                trans_quota.endDbAccess();
                            }
                        }
                    }).intValue();
        } catch (PrivilegedActionException epriv) {
            try {
                throw epriv.getCause();
            } catch (BaseException e) {
                throw (BaseException) e;
            } catch (GeneralSecurityException e) {
                throw (GeneralSecurityException) e;
            } catch (RemoteException e) {
                throw (RemoteException) e;
            } catch ( Throwable e) {
                throw new AssertionFailedException("Failed to increment quota", e);
            }
        }
    }
}
