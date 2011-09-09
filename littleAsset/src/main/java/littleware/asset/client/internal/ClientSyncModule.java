/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.client.internal;

import littleware.asset.client.AssetRef;
import littleware.asset.client.AssetLibrary;
import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import java.util.Collection;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.base.event.LittleListener;
import littleware.bootstrap.AppBootstrap;
import littleware.bootstrap.AppBootstrap.AppProfile;
import littleware.bootstrap.AppModule;
import littleware.bootstrap.AppModuleFactory;
import littleware.bootstrap.helper.AbstractAppModule;
import littleware.security.auth.LittleSession;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Module activates services that attempt to keep client cache
 * and AssetRef library in sync with server state.
 */
public class ClientSyncModule extends AbstractAppModule {

    private static final Logger log = Logger.getLogger(ClientSyncModule.class.getName());
    private static final Collection<Class<? extends LittleListener>> serviceListeners;

    static {
        final ImmutableList.Builder<Class<? extends LittleListener>> builder =
                ImmutableList.builder();
        serviceListeners = builder.add(AssetLibServiceListener.class
                ).add( ServerSync.class
                ).build();
    }

    public static class Factory implements AppModuleFactory {

        @Override
        public AppModule build(AppProfile profile) {
            return new ClientSyncModule( profile );
        }

    }

    private ClientSyncModule(AppBootstrap.AppProfile profile) {
        super(profile);
    }

    @Override
    public Class<Activator> getActivator() {
        return Activator.class;
    }

    @Override
    public void configure( Binder binder ) {
        binder.bind(AssetLibrary.class).to(SimpleAssetLibrary.class).in(Scopes.SINGLETON);
        binder.bind( ServerSync.class ).to( SimpleServerSync.class ).in( Scopes.SINGLETON );
    }

    /**
     * Setup bootstrap with preconfigured ClientServiceGuice module.
     *
     *

    public ClientSyncModule() {

    new LgoGuice(),
    new littleware.apps.swingclient.StandardSwingGuice(),
    new littleware.apps.misc.StandardMiscGuice(),


    this.getOSGiActivator().add(AssetLibServiceListener.class);
    this.getOSGiActivator().add(CacheActivator.class);
    this.getOSGiActivator().add(SyncWithServer.class);
    this.getOSGiActivator().add(LgoActivator.class);
    this.getOSGiActivator().add(ClientSessionActivator.class);

    this.clientGuice = clientGuice;
     *
     *
    }
     */

    /**
     * Just inject the LittleSession into things that
     * want an asset-model injected into the constructor
     */
    @Provides
    public AssetRef getDefaultModel(
            LittleSession session,
            AssetLibrary libAsset) {
        return libAsset.syncAsset(session);
    }

    @Singleton
    public static class Activator implements BundleActivator{

        private final ScheduledExecutorService execSchedule;
        private final ServerSync     serverSync;

        @Inject
        public Activator(
                ServerSync     serverSync,
                ScheduledExecutorService execSchedule) {
            this.serverSync = serverSync;
            this.execSchedule = execSchedule;
        }

        @Override
        public void start(BundleContext bundle) throws Exception {
            final ScheduledFuture<?> handle = execSchedule.scheduleWithFixedDelay(
                    new Runnable() {

                @Override
                public void run() {
                    try {
                        serverSync.syncWithServer();
                    } catch ( Exception ex ) {
                        log.log( Level.WARNING, "Server sync failed", ex );
                    }
                }
            }, 60, 20, TimeUnit.SECONDS);
        }

        @Override
        public void stop(BundleContext bundle) throws Exception {
        }

    }
}
