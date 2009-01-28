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
    private final TransactionManager   omgr_trans;
    
    @Inject
    public SimpleQuotaUtil( TransactionManager mgr_trans ) {
        omgr_trans = mgr_trans;
    }

    /**
     * Get a Subject representing the littleware admin
     */
    private Subject getAdmin( AssetSearchManager m_search ) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        if (null == oj_admin) {
            try {
                LittleUser p_admin = (LittleUser) m_search.getByName(AccountManager.LITTLEWARE_ADMIN, SecurityAssetType.USER);
                Set<Principal> v_users = new HashSet<Principal>();

                v_users.add(p_admin);
                oj_admin = new Subject(true, v_users, new HashSet<Object>(), new HashSet<Object>());
            } catch (NoSuchThingException e) {
                throw new AssertionFailedException("LITTLEWARE_ADMIN should exist, caught: " + e, e);
            } catch (GeneralSecurityException e) {
                throw new AssertionFailedException("LITTLEWARE_ADMIN should be accessible, but caught: " + e,
                        e);
            }
        }
        return oj_admin;
    }

    public Quota getQuota(LittleUser p_user, AssetSearchManager m_search ) throws BaseException,
            GeneralSecurityException, RemoteException {
        Map<String, UUID> v_quotas = m_search.getAssetIdsFrom(p_user.getObjectId(),
                SecurityAssetType.QUOTA);
        UUID u_child = v_quotas.get("littleware_quota");
        if (null == u_child) {
            return null;
        }

        return (Quota) m_search.getAssetOrNull(u_child);
    }


    public int incrementQuotaCount( final LittleUser p_user,
            final AssetManager m_asset,
            final AssetSearchManager m_search
            ) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {

        try {
            return Subject.doAs(getAdmin(m_search),
                    new PrivilegedExceptionAction<Integer>() {

                        public Integer run() throws Exception {
                            int i_ops_left = -1;  // quota ops left
                            List<Quota> v_chain = new ArrayList<Quota>();
                            LittleTransaction trans_quota = omgr_trans.getThreadTransaction();
                            Date t_now = new Date();

                            trans_quota.startDbAccess();
                            try {
                                for (Quota a_quota = getQuota(p_user, m_search);
                                        null != a_quota;
                                        a_quota = (null != a_quota.getToId()) ? ((Quota) m_search.getAsset(a_quota.getToId()))
                                                : null) {
                                    v_chain.add(a_quota);
                                    if ((null != a_quota.getEndDate()) && (a_quota.getEndDate().getTime() < t_now.getTime())) {
                                        long l_period = a_quota.getEndDate().getTime() -
                                                a_quota.getStartDate().getTime();

                                        if (l_period < 1000000) {
                                            l_period = 1000000;
                                        }
                                        Date t_end = new Date(l_period + t_now.getTime());
                                        a_quota.setStartDate(t_now);
                                        a_quota.setEndDate(t_end);
                                        a_quota.setQuotaCount(0);
                                    } else if ((a_quota.getQuotaLimit() >= 0) && (a_quota.getQuotaLimit() < a_quota.getQuotaCount())) {
                                        throw new QuotaException("Quota exceeded: " + a_quota.getQuotaLimit() +
                                                " less than " + a_quota.getQuotaCount());
                                    }

                                    int i_left = a_quota.getQuotaLimit() -
                                            a_quota.getQuotaCount();

                                    if ((i_left >= 0) && (i_left < i_ops_left)) {
                                        i_ops_left = i_left;
                                    }
                                }

                                for (Quota a_quota : v_chain) {
                                    a_quota.incrementQuotaCount();
                                    // don't worry about missed transactions
                                    a_quota.setTransactionCount(0);

                                    olog.log(Level.FINE, "Incrementing quota count on " + a_quota.getObjectId());
                                    a_quota = (Quota) m_asset.saveAsset(a_quota, "update quota count");
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
