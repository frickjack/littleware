/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.internal;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import littleware.asset.Asset;
import littleware.asset.server.LittleContext;
import littleware.asset.server.LittleTransaction;
import littleware.asset.server.ServerSearchManager;
import littleware.base.BaseException;
import littleware.base.Maybe;
import littleware.base.Option;
import littleware.security.AccountManager;
import littleware.security.LittleAcl;
import littleware.security.LittleGroup;
import littleware.security.LittlePermission;
import littleware.security.LittleUser;
import littleware.security.auth.LittleSession;
import org.joda.time.DateTime;

/**
 * Simple implementation of LittleContext.ContextFactory just
 * calls through to a ServerSearchManager to load the session
 * and user assets associated with a given sessionId,
 * and just manufactures (mocks) admin credentials.
 */
@Singleton
public class SimpleContextFactory implements LittleContext.ContextFactory {

    private static final Logger log = Logger.getLogger(SimpleContextFactory.class.getName());
    private final ServerSearchManager search;
    private final Provider<LittleSession.Builder> sessionBuilder;
    private final Provider<LittleUser.Builder> userBuilder;
    private final Provider<LittleTransaction> transactionProvider;
    private final LittleContext adminContext;
    private LittleGroup adminGroup = null;
    private DateTime lastUpdate = new DateTime();

    private boolean isAdmin(LittleUser user) {
        final DateTime now = new DateTime();
        if ((null == adminGroup) || lastUpdate.plusSeconds(10).isAfter(now)) {
            synchronized (this) {
                try {
                    adminGroup = search.getAsset(adminContext, AccountManager.UUID_ADMIN_GROUP).get().narrow();
                    lastUpdate = now;
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Failed to load admin group", ex);
                    return false;
                }
            }
        }
        return adminGroup.isMember(user);
    }

    @Inject
    public SimpleContextFactory(
            ServerSearchManager search,
            Provider<LittleSession.Builder> sessionBuilder,
            Provider<LittleUser.Builder> userBuilder,
            Provider<LittleTransaction> transactionProvider) {
        this.search = search;
        this.sessionBuilder = sessionBuilder;
        this.userBuilder = userBuilder;
        this.transactionProvider = transactionProvider;
        final UUID homeId = UUID.randomUUID();
        final LittleSession adminSession = sessionBuilder.get().name("bogusAdminSession").homeId(homeId).build();
        final LittleUser admin = userBuilder.get().name(AccountManager.LITTLEWARE_ADMIN).parentId(homeId).homeId(homeId).id(AccountManager.UUID_ADMIN).build();
        adminContext = new SimpleContext(adminSession, admin, transactionProvider.get(), true, search );
    }

    @Override
    public LittleContext build(UUID sessionId) {
        try {
            final LittleSession session = search.getAsset(adminContext, sessionId).get().narrow();
            final LittleUser user = search.getAsset(adminContext, session.getOwnerId()).get().narrow();
            return new SimpleContext(session, user, transactionProvider.get(), isAdmin(user), search );
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to setup session context", ex);
        }
    }

    @Override
    public LittleContext getAdminContext() {
        return adminContext;
    }

    //----------------------------------------------
    public static class SimpleContext implements LittleContext {

        private final LittleSession session;
        private final LittleUser caller;
        private final LittleTransaction transaction;
        private final Map<UUID, Asset> lookupCache;
        private final Map<UUID, Asset> savedCache = new HashMap<UUID, Asset>();
        private final Subject subject;
        private final boolean isAdmin;
        private final ServerSearchManager search;

        public SimpleContext(LittleSession session,
                LittleUser caller,
                LittleTransaction transaction,
                boolean isAdmin,
                ServerSearchManager search) {
            this.session = session;
            this.caller = caller;
            this.transaction = transaction;
            this.subject = new Subject(true, Collections.singleton(caller), Collections.singleton(session), Collections.emptySet());
            this.lookupCache = transaction.startDbAccess();
            this.isAdmin = isAdmin;
            this.search = search;
        }

        @Override
        public LittleSession getSession() {
            return session;
        }

        @Override
        public LittleUser getCaller() {
            return caller;
        }

        @Override
        public Subject getSubject() {
            return subject;
        }

        @Override
        public LittleTransaction getTransaction() {
            return transaction;
        }

        @Override
        public Option<Asset> checkCache(UUID id) {
            return Maybe.emptyIfNull(lookupCache.get(id));
        }

        @Override
        public void addToCache(Asset asset) {
            lookupCache.put(asset.getId(), asset);
        }

        @Override
        public void savedAsset(Asset asset) {
            savedCache.put(asset.getId(), asset);
        }

        @Override
        public Option<Asset> checkIfSaved(UUID id) {
            return Maybe.emptyIfNull(savedCache.get(id));
        }

        @Override
        public boolean isAdmin() {
            return isAdmin;
        }
        private final Map<UUID, LittleAcl> aclCache = new HashMap<UUID, LittleAcl>();

        @Override
        public boolean checkPermission(LittlePermission permission, UUID aclId) throws BaseException, GeneralSecurityException {
            if (null == aclId) {
                return false;
            }
            LittleAcl acl;
            synchronized (aclCache) {
                acl = aclCache.get(aclId);
            }
            if (null == acl) {
                Option<Asset> maybe = search.getAsset(this, aclId);
                if ((!maybe.isSet())
                        || (!maybe.get().getAssetType().equals(LittleAcl.ACL_TYPE))) {
                    return false;
                }
                acl = maybe.get().narrow();
                synchronized (aclCache) {
                    aclCache.put(acl.getId(), acl);
                }
            }
            return acl.checkPermission(caller, permission);
        }
    }
}
