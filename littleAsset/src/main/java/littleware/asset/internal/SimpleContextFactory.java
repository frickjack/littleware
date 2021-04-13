package littleware.asset.internal;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.security.GeneralSecurityException;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import littleware.asset.Asset;
import littleware.asset.LittleContext;
import littleware.asset.LittleTransaction;
import littleware.asset.ServerSearchManager;
import littleware.base.BaseException;
import littleware.security.AccountManager;
import littleware.security.LittleAcl;
import littleware.security.LittleGroup;
import littleware.security.LittlePermission;
import littleware.security.LittleUser;
import littleware.security.auth.LittleSession;
import littleware.security.auth.SessionExpiredException;


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
    private LittleGroup adminGroup = null;
    private ZonedDateTime lastUpdate = ZonedDateTime.now();
    private final LittleSession adminSession;
    private final LittleUser adminUser;

    private boolean isAdmin( LittleContext ctx, LittleUser user) {
        final ZonedDateTime now = ZonedDateTime.now();
        if ((null == adminGroup) || lastUpdate.plusSeconds(10).isAfter(now)) {
            synchronized (this) {
                try {
                    adminGroup = search.getAsset(ctx, AccountManager.UUID_ADMIN_GROUP, -1L ).getAsset().get().narrow();
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
            Provider<LittleTransaction> transactionProvider
            ) {
        this.search = search;
        this.sessionBuilder = sessionBuilder;
        this.userBuilder = userBuilder;
        this.transactionProvider = transactionProvider;
        final UUID homeId = UUID.randomUUID();
        adminSession = sessionBuilder.get().name("bogusAdminSession").homeId(homeId).build();
        adminUser = userBuilder.get().name(AccountManager.LITTLEWARE_ADMIN).parentId(homeId).homeId(homeId).id(AccountManager.UUID_ADMIN).build();
    }

    @Override
    public LittleContext build(UUID sessionId) {
        try {
            final LittleContext adminContext = buildAdminContext();
            final LittleSession session = search.getAsset(adminContext, sessionId, -1L ).getAsset().get().narrow();
            final LittleUser user = search.getAsset(adminContext, session.getOwnerId(), -1L ).getAsset().get().narrow();
            
            if ( session.getEndDate() != null ) {
              final Date now = new Date();
              if ( session.getEndDate().getTime() < now.getTime() ) {
                throw new SessionExpiredException();
              }
            }
            return new SimpleContext(session, user, adminContext.getTransaction(), isAdmin( adminContext, user), search, adminContext );
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to setup session context", ex);
        }
    }

    @Override
    public LittleContext buildAdminContext() {
        return new SimpleContext( adminSession, adminUser, transactionProvider.get(), true, search, null );
    }

    //----------------------------------------------
    public static class SimpleContext implements LittleContext {

        private final LittleSession session;
        private final LittleUser caller;
        private final LittleTransaction transaction;
        private final Map<UUID, Asset> lookupCache;
        private final Map<UUID, Asset> savedCache = new HashMap<>();
        private final Subject subject;
        private final ServerSearchManager search;
        private final LittleContext adminCtx;
        private final boolean isAdmin;

        public SimpleContext(LittleSession session,
                LittleUser caller,
                LittleTransaction transaction,
                boolean isAdmin,
                ServerSearchManager search,
                LittleContext adminCtx ) {
            this.session = session;
            this.caller = caller;
            this.transaction = transaction;
            this.subject = new Subject(true, Collections.singleton(caller), Collections.singleton(session), Collections.emptySet());
            this.lookupCache = transaction.getCache();
            this.isAdmin = isAdmin;
            this.search = search;
            this.adminCtx = adminCtx;
        }

        @Override
        public LittleContext getAdminContext() {
            if ( null == adminCtx ) {
                return this;
            } else {
                return adminCtx;
            }
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
        public Optional<Asset> checkCache(UUID id) {
            return Optional.ofNullable(lookupCache.get(id));
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
        public Optional<Asset> checkIfSaved(UUID id) {
            return Optional.ofNullable(savedCache.get(id));
        }

        @Override
        public boolean isAdmin() {
            return isAdmin;
        }
        private final Map<UUID, LittleAcl> aclCache = new HashMap<>();

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
                final Optional<Asset> maybe = search.getAsset(this, aclId, -1L ).getAsset();
                if ((!maybe.isPresent())
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
