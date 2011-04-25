/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.client;

import littleware.apps.client.internal.SimpleServerSync;
import littleware.apps.client.internal.SimpleAssetModelLibrary;
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
import littleware.asset.client.bootstrap.helper.AbstractClientModule;
import littleware.asset.client.bootstrap.SessionModule;
import littleware.asset.client.bootstrap.SessionModuleFactory;
import littleware.base.event.LittleListener;
import littleware.bootstrap.AppBootstrap;
import littleware.bootstrap.AppBootstrap.AppProfile;
import littleware.security.auth.LittleSession;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Module activates services that attempt to keep client cache
 * and AssetModel library in sync with server state.
 */
public class ClientSyncModule extends AbstractClientModule {

    private static final Logger log = Logger.getLogger(ClientSyncModule.class.getName());
    private static final Collection<Class<? extends LittleListener>> serviceListeners;

    static {
        final ImmutableList.Builder<Class<? extends LittleListener>> builder =
                ImmutableList.builder();
        serviceListeners = builder.add(AssetModelServiceListener.class
                ).add( ServerSync.class
                ).build();
    }

    public static class Factory implements SessionModuleFactory {

        @Override
        public SessionModule build(AppProfile profile) {
            return new ClientSyncModule( profile );
        }

    }

    private ClientSyncModule(AppBootstrap.AppProfile profile) {
        super(profile, serviceListeners);
    }

    @Override
    public Class<Activator> getActivator() {
        return Activator.class;
    }

    @Override
    public void configure( Binder binder ) {
        binder.bind(AssetModelLibrary.class).to(SimpleAssetModelLibrary.class).in(Scopes.SINGLETON);
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


    this.getOSGiActivator().add(AssetModelServiceListener.class);
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
    public AssetModel getDefaultModel(
            LittleSession session,
            AssetModelLibrary libAsset) {
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
