/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.bootstrap.client;

import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.AssetType;
import littleware.asset.client.AssetLoadEvent;
import littleware.asset.client.ClientCache;
import littleware.asset.client.LittleService;
import littleware.asset.client.LittleServiceEvent;
import littleware.asset.client.LittleServiceListener;
import littleware.asset.client.SimpleClientCache;
import littleware.asset.pickle.AssetHumanPickler;
import littleware.asset.pickle.HumanPicklerProvider;
import littleware.base.AssertionFailedException;
import littleware.base.Maybe;
import littleware.base.PropertiesGuice;
import littleware.bootstrap.client.AppBootstrap.AppProfile;
import littleware.security.SecurityAssetType;
import littleware.security.auth.ServiceType;
import littleware.security.auth.SessionHelper;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;


public class AssetClientModule extends AbstractClientModule {
    private static final Logger log = Logger.getLogger( AssetClientModule.class.getName() );
    
    //-------------------------
    public static class Factory implements ClientModuleFactory {

        private final Collection<AssetType> typeSet;

        {
            final ImmutableList.Builder<AssetType> builder = ImmutableList.builder();
            typeSet = builder.add(AssetType.GENERIC).add(AssetType.HOME).add(AssetType.LINK).add(AssetType.LOCK).add(SecurityAssetType.ACL).add(SecurityAssetType.ACL_ENTRY).add(SecurityAssetType.GROUP).add(SecurityAssetType.GROUP_MEMBER).add(SecurityAssetType.PRINCIPAL).add(SecurityAssetType.QUOTA).add(SecurityAssetType.SERVICE_STUB).add(SecurityAssetType.SESSION).add(SecurityAssetType.USER).build();
        }
        private final Collection<Class<? extends LittleServiceListener>> listenerSet;

        {
            final ImmutableList.Builder<Class<? extends LittleServiceListener>> builder =
                    ImmutableList.builder();
            listenerSet = builder.add(SimpleClientCache.class).build();
        }
        private final Collection<ServiceType> serviceSet;

        {
            final ImmutableList.Builder<ServiceType> builder = ImmutableList.builder();
            serviceSet = builder.add(ServiceType.ACCOUNT_MANAGER).add(ServiceType.ASSET_MANAGER).add(ServiceType.ASSET_SEARCH).build();
        }

        @Override
        public ClientModule build(AppProfile profile) {
            return new AssetClientModule(profile, typeSet, serviceSet, listenerSet);
        }
    }

    //-------------------------------------
    
    /**
     * Utility activator takes care of shutting down the
     * bootstraps the session helper, and takes care of client-side
     * injection of just-loaded assets.
     */
    public static class Activator
            implements BundleActivator, LittleServiceListener {

        private final SessionHelper helper;
        private final Injector injector;
        private final ClientBootstrap bootstrap;
        private final ClientCache clientCache;

        @Inject
        public Activator(Injector injector, SessionHelper helper, 
                ClientBootstrap bootstrap,
                ClientCache clientCache ) {
            this.helper = helper;
            this.injector = injector;
            ((LittleService) helper).addServiceListener(this);
            this.bootstrap = bootstrap;
            this.clientCache = clientCache;
        }

        @Override
        public void start(BundleContext ctx) throws Exception {
            final LittleService helperService = (LittleService) helper;
            helperService.start(ctx);
            ctx.registerService( ClientCache.class.getName(), clientCache, new Properties() );
            for( ClientModule module : bootstrap.getModuleSet() ) {
                for( Class<? extends LittleServiceListener> listenerClass : module.getServiceListeners() ) {
                    log.log( Level.FINE, "Registering client service listener: " + listenerClass.getName() );
                    helperService.addServiceListener( injector.getInstance(listenerClass) );
                }
            }
        }

        @Override
        public void stop(BundleContext ctx) throws Exception {
            ((LittleService) helper).stop(ctx);
        }

        @Override
        public void receiveServiceEvent(LittleServiceEvent eventBase) {
            if (eventBase instanceof AssetLoadEvent) {
                final AssetLoadEvent eventLoad = (AssetLoadEvent) eventBase;
                injector.injectMembers(eventLoad.getAsset());
            }
        }
    }

    // --------------------------------------

    private AssetClientModule(AppBootstrap.AppProfile profile,
            Collection<AssetType> assetTypes,
            Collection<ServiceType> serviceTypes,
            Collection<Class<? extends LittleServiceListener>> serviceListeners) {
        super(profile, assetTypes, serviceTypes, serviceListeners);
    }

    @Override
    public void configure(Binder binder) {
        try {
            (new PropertiesGuice()).configure(binder);
            binder.bind(AssetHumanPickler.class).toProvider(HumanPicklerProvider.class);
        } catch (IOException ex) {
            throw new AssertionFailedException("Failed to load littleware.properties", ex);
        }
    }

    @Override
    public Maybe<Class<Activator>> getActivator() {
        return Maybe.something(Activator.class);
    }
}
