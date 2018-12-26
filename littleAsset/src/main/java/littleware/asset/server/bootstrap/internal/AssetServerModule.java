package littleware.asset.server.bootstrap.internal;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.*;
import littleware.asset.internal.LittleAssetModule.ClientConfig.RemoteMethod;
import littleware.asset.internal.RemoteAssetManager;
import littleware.asset.internal.RemoteSearchManager;
import littleware.asset.server.AssetSpecializer;
import littleware.asset.server.AssetSpecializerRegistry;
import littleware.asset.server.LittleContext;
import littleware.asset.server.LittleTransaction;
import littleware.asset.server.NullAssetSpecializer;
import littleware.asset.server.ServerAssetManager;
import littleware.asset.server.ServerScannerFactory;
import littleware.asset.server.ServerSearchManager;
import littleware.asset.server.bootstrap.AbstractServerModule;
import littleware.security.server.QuotaUtil;
import littleware.asset.server.internal.DbAssetManager;
import littleware.asset.server.internal.SimpleScannerFactory;
import littleware.asset.server.internal.DbSearchManager;
import littleware.asset.server.internal.RmiAssetManager;
import littleware.asset.server.internal.RmiSearchManager;
import littleware.security.server.internal.SimpleQuotaUtil;
import littleware.asset.server.internal.SimpleSpecializerRegistry;
import littleware.asset.server.db.DbCommandManager;
import littleware.base.PropertiesGuice;
import littleware.asset.server.bootstrap.ServerModule;
import littleware.asset.server.internal.SimpleContextFactory;
import littleware.bootstrap.AppBootstrap;
import littleware.bootstrap.AppModuleFactory;
import littleware.bootstrap.LittleBootstrap;
import littleware.bootstrap.LittleModule;
import littleware.security.LittleAcl;
import littleware.security.LittleAclEntry;
import littleware.security.LittleGroup;
import littleware.security.LittlePrincipal;
import littleware.security.LittleUser;
import littleware.security.Quota;
import littleware.security.auth.internal.RemoteSessionManager;
import littleware.security.auth.server.internal.SimpleSessionManager;
import littleware.security.server.internal.SimpleAccountManager;
import littleware.security.server.internal.SimpleAclManager;


/**
 * Simple server-side bootstrap module for littleware.asset and littleware.security
 */
public class AssetServerModule extends AbstractServerModule {

    private static final Logger log = Logger.getLogger(AssetServerModule.class.getName());

    private AssetServerModule(AppBootstrap.AppProfile profile,
            Map<AssetType, Class<? extends AssetSpecializer>> typeMap
            ) {
        super(profile, typeMap, emptyServerListeners);
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(ServerAssetManager.class).to(DbAssetManager.class).in(Scopes.SINGLETON);
        binder.bind( ServerScannerFactory.class ).to( SimpleScannerFactory.class ).in( Scopes.SINGLETON );
        binder.bind(ServerSearchManager.class).to(DbSearchManager.class).in(Scopes.SINGLETON);
        binder.bind(QuotaUtil.class).to(SimpleQuotaUtil.class).in(Scopes.SINGLETON);
        binder.bind(AssetSpecializerRegistry.class).to(SimpleSpecializerRegistry.class).in(Scopes.SINGLETON);
        binder.bind( RemoteSessionManager.class).to(SimpleSessionManager.class).in(Scopes.SINGLETON);
        binder.bind( RemoteSearchManager.class ).to( RmiSearchManager.class ).in( Scopes.SINGLETON );
        binder.bind( RemoteAssetManager.class ).to( RmiAssetManager.class ).in( Scopes.SINGLETON );
        binder.bind( LittleContext.ContextFactory.class ).to( SimpleContextFactory.class ).in( Scopes.SINGLETON );

        try {
            PropertiesGuice.build().configure(binder);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to access littleware.properties file", ex);
        }
    }

    /**
     * Server lifecycle listener - should register/unregister with service registries ...
     */
    public static class Activator implements LifecycleCallback {

        private final RemoteSessionManager sessionMgr;
        private final RemoteAssetManager assetMgr;
        private final RemoteSearchManager searchMgr;

        @Inject
        public Activator(
                LittleBootstrap bootstrap,
                AssetSpecializerRegistry assetRegistry,
                RemoteSessionManager sessionMgr,
                RemoteAssetManager   assetMgr,
                RemoteSearchManager  searchMgr,
                DbCommandManager dbManager,
                LittleContext.ContextFactory ctxProvider,
                Injector injector
                ) {
            this.sessionMgr = sessionMgr;
            boolean rollback = true;
            // setup an overall transaction for the asset type auto-register code
            final LittleContext ctx = ctxProvider.buildAdminContext();
            final LittleTransaction transaction = ctx.getTransaction();
            transaction.startDbUpdate();
            try {
                // Register asset specializers
                for (LittleModule scan : bootstrap.getModuleSet()) {
                    if (scan instanceof ServerModule) {
                        final ServerModule module = (ServerModule) scan;
                        for (Map.Entry<AssetType, Class<? extends AssetSpecializer>> entry : module.getAssetTypes().entrySet()) {
                            assetRegistry.registerService(entry.getKey(), injector.getInstance(entry.getValue()));
                            dbManager.makeTypeChecker( transaction ).saveObject(entry.getKey());
                        }
                    } 
                }
                rollback = false;
            } catch (SQLException ex) {
                throw new IllegalStateException("Failed to auto-register asset types", ex);
            } finally {
                transaction.endDbUpdate(rollback);
            }
            this.assetMgr = assetMgr;
            this.searchMgr = searchMgr;
        }

        @Override
        public void startUp() {
            log.log(Level.INFO, "littleware RMI and REST start ok");
        }

        @Override
        public void shutDown(){
            log.log(Level.INFO, "littleware shutdown ok");
        }
    }

    @Override
    public Optional<Class<Activator>> getCallback() {
        return Optional.of( Activator.class );
    }



    public static class Factory implements AppModuleFactory {

        private final Map<AssetType, Class<? extends AssetSpecializer>> typeMap;

        {
            final ImmutableMap.Builder<AssetType, Class<? extends AssetSpecializer>> builder = ImmutableMap.builder();
            for (AssetType assetType : Arrays.asList(GenericAsset.GENERIC, LittleHome.HOME_TYPE, LinkAsset.LINK_TYPE)) {
                builder.put(assetType, NullAssetSpecializer.class);
            }
            for (AssetType assetType : Arrays.asList(LittlePrincipal.PRINCIPAL_TYPE,
                    LittleGroup.GROUP_TYPE,
                    Quota.QUOTA_TYPE,
                    LittleUser.USER_TYPE)) {
                builder.put(assetType, SimpleAccountManager.class);
            }
            for (AssetType assetType : Arrays.asList(LittleAcl.ACL_TYPE,
                    LittleAclEntry.ACL_ENTRY)) {
                builder.put(assetType, SimpleAclManager.class);
            }


            typeMap = builder.build();
        }

        @Override
        public ServerModule build(AppBootstrap.AppProfile profile) {
            log.log( Level.FINE, "Flipping LittleAssetModule over to InMemory RemoteMethod" );
            littleware.asset.internal.LittleAssetModule.getClientConfig().setRemoteMethod(RemoteMethod.InMemory);
            return new AssetServerModule(profile, typeMap);
        }
    }
}
