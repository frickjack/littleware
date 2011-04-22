/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.client.bootstrap.internal;

import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.client.AssetLoadEvent;
import littleware.asset.client.ClientCache;
import littleware.asset.client.LittleService;
import littleware.asset.client.LittleServiceEvent;
import littleware.asset.client.LittleServiceListener;
import littleware.asset.client.SimpleClientCache;
import littleware.asset.client.bootstrap.AbstractClientModule;
import littleware.asset.client.bootstrap.ClientBootstrap;
import littleware.asset.client.bootstrap.ClientModule;
import littleware.asset.client.bootstrap.ClientModuleFactory;
import littleware.bootstrap.AppBootstrap;
import littleware.bootstrap.AppBootstrap.AppProfile;
import littleware.bootstrap.LittleModule;
import littleware.security.auth.SessionHelper;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class AssetClientModule extends AbstractClientModule {

    private static final Logger log = Logger.getLogger(AssetClientModule.class.getName());

    @Override
    public void configure(Binder binder) {
    }

    //-------------------------
    public static class Factory implements ClientModuleFactory {

        private final Collection<Class<? extends LittleServiceListener>> listenerSet;

        {
            final ImmutableList.Builder<Class<? extends LittleServiceListener>> builder =
                    ImmutableList.builder();
            listenerSet = builder.add(SimpleClientCache.class).build();
        }

        @Override
        public ClientModule build(AppProfile profile) {
            return new AssetClientModule(profile, listenerSet);
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
                ClientCache clientCache) {
            this.helper = helper;
            this.injector = injector;
            this.bootstrap = bootstrap;
            this.clientCache = clientCache;
            ((LittleService) helper).addServiceListener(this);
        }

        @Override
        public void start(BundleContext ctx) throws Exception {
            final LittleService helperService = (LittleService) helper;
            helperService.start(ctx);
            ctx.registerService(ClientCache.class.getName(), clientCache, new Properties());
            for (LittleModule appModule : bootstrap.getModuleSet()) {
                if (appModule instanceof ClientModule) {
                    final ClientModule module = (ClientModule) appModule;
                    for (Class<? extends LittleServiceListener> listenerClass : module.getServiceListeners()) {
                        log.log(Level.FINE, "Registering client service listener: {0}", listenerClass.getName());
                        helperService.addServiceListener(injector.getInstance(listenerClass));
                    }
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
            Collection<Class<? extends LittleServiceListener>> serviceListeners) {
        super(profile, serviceListeners);
    }

    @Override
    public Class<Activator> getActivator() {
        return Activator.class;
    }
}
