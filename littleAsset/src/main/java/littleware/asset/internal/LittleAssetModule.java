/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.internal;

import littleware.asset.client.internal.InMemorySearchMgrProxy;
import littleware.asset.client.internal.InMemoryAssetMgrProxy;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import littleware.asset.AssetPathFactory;
import littleware.asset.AssetTreeTemplate;
import littleware.asset.GenericAsset;
import littleware.asset.IdWithClock;
import littleware.asset.LinkAsset;
import littleware.asset.LittleHome;
import littleware.asset.TreeNode;
import littleware.asset.client.internal.RemoteAssetMgrProxy;
import littleware.asset.client.internal.RemoteSearchMgrProxy;
import littleware.asset.client.internal.RmiAssetMgrProxy;
import littleware.asset.client.internal.RmiSearchMgrProxy;
import littleware.asset.gson.GsonAssetAdapter;
import littleware.asset.gson.LittleGsonFactory;
import littleware.asset.gson.internal.GenericAdapter;
import littleware.asset.gson.internal.GsonProvider;
import littleware.asset.gson.internal.HomeAdapter;
import littleware.asset.gson.internal.LinkAdapter;
import littleware.asset.gson.internal.TreeNodeAdapter;
import littleware.asset.pickle.HumanPicklerProvider;
import littleware.asset.pickle.XmlPicklerProvider;
import littleware.asset.pickle.internal.SimpleHumanRegistry;
import littleware.asset.pickle.internal.SimpleXmlRegistry;
import littleware.asset.spi.AssetProviderRegistry;
import littleware.asset.spi.internal.SimpleAssetRegistry;
import littleware.bootstrap.AppBootstrap.AppProfile;
import littleware.bootstrap.AppModule;
import littleware.bootstrap.AppModuleFactory;
import littleware.bootstrap.helper.AbstractAppModule;
import littleware.security.auth.client.internal.InMemorySessionMgrProxy;
import littleware.security.auth.client.internal.RemoteSessionMgrProxy;
import littleware.security.auth.client.internal.RetryRemoteSessionMgr;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Module binds and registers littleware.asset asset types
 */
public class LittleAssetModule extends AbstractAppModule {

    public static class AppFactory implements AppModuleFactory {

        @Override
        public AppModule build(AppProfile profile) {
            return new LittleAssetModule(profile);
        }
    }

    //---------------------------------------
    /**
     * Client-side configuration
     */
    public static class ClientConfig {

        /**
         * Remoting method - default is InMemory if an in-process server
         * is running, otherwise RMI.  REST is not yet fully implemented.
         */
        public enum RemoteMethod {

            InMemory, RMI, REST
        }
        private RemoteMethod mode = RemoteMethod.RMI;

        public RemoteMethod getRemoteMethod() {
            return mode;
        }

        public void setRemoteMethod(RemoteMethod value) {
            mode = value;
            if (value.equals(RemoteMethod.REST)) {
                throw new UnsupportedOperationException("REST mode not yet available");
            }
        }
    }
    private static ClientConfig clientConfig = new ClientConfig();

    public static ClientConfig getClientConfig() {
        return clientConfig;
    }

    //-------------------------------------
    public LittleAssetModule(AppProfile profile) {
        super(profile);
    }

    public static class Activator implements BundleActivator {

        @Inject
        public Activator(AssetProviderRegistry assetRegistry,
                Provider<TreeNode.TreeNodeBuilder> nodeProvider,
                Provider<GenericAsset.GenericBuilder> genericProvider,
                Provider<LinkAsset.LinkBuilder> linkProvider,
                Provider<LittleHome.HomeBuilder> homeProvider,
                LittleGsonFactory gsonFactory,
                HomeAdapter gsonHomeAdapter,
                LinkAdapter gsonLinkAdapter,
                TreeNodeAdapter gsonTreeNodeAdapter,
                GenericAdapter gsonGenericAdapter) {
            assetRegistry.registerService(LittleHome.HOME_TYPE, homeProvider);
            assetRegistry.registerService(TreeNode.TREE_NODE_TYPE, nodeProvider);
            assetRegistry.registerService(GenericAsset.GENERIC, genericProvider);
            assetRegistry.registerService(LinkAsset.LINK_TYPE, linkProvider);
            for (GsonAssetAdapter adapter : new GsonAssetAdapter[]{
                        gsonHomeAdapter, gsonLinkAdapter, gsonTreeNodeAdapter, gsonGenericAdapter
                    }) {
                gsonFactory.registerAdapter(adapter);
            }
        }

        @Override
        public void start(BundleContext bc) throws Exception {
        }

        @Override
        public void stop(BundleContext bc) throws Exception {
        }
    }

    @Override
    public Class<? extends BundleActivator> getActivator() {
        return Activator.class;
    }
    
    @Override
    public void configure(Binder binder) {
        binder.bind(LittleHome.HomeBuilder.class).to(LittleHomeBuilder.class);
        binder.bind(GenericAsset.GenericBuilder.class).to(SimpleGenericBuilder.class);
        binder.bind(TreeNode.TreeNodeBuilder.class).to(SimpleTreeNodeBuilder.class);
        binder.bind(LinkAsset.LinkBuilder.class).to(SimpleLinkBuilder.class);
        binder.bind(IdWithClock.Builder.class).to(IdWithClockBuilder.class);
        binder.bind(AssetPathFactory.class).to(SimpleAssetPathFactory.class);
        binder.bind(AssetTreeTemplate.TemplateBuilder.class).to(SimpleTemplateBuilder.class).in(Scopes.SINGLETON);
        binder.bind(HumanPicklerProvider.class).to(SimpleHumanRegistry.class).in(Scopes.SINGLETON);
        binder.bind(XmlPicklerProvider.class).to(SimpleXmlRegistry.class).in(Scopes.SINGLETON);
        binder.bind(AssetProviderRegistry.class).to(SimpleAssetRegistry.class).in(Scopes.SINGLETON);
        binder.bind(LittleGsonFactory.class).to(GsonProvider.class).in(Scopes.SINGLETON);
        binder.bind(RmiSearchMgrProxy.class).in(Scopes.SINGLETON);
        binder.bind(RmiAssetMgrProxy.class).in(Scopes.SINGLETON);
        
        // Bind client method of connecting with server
        switch (getClientConfig().getRemoteMethod()) {
            case RMI: {
                binder.bind(RemoteAssetMgrProxy.class).to(RmiAssetMgrProxy.class).in(Scopes.SINGLETON);
                binder.bind(RemoteSearchMgrProxy.class).to(RmiSearchMgrProxy.class).in(Scopes.SINGLETON);
                binder.bind( RemoteSessionMgrProxy.class ).to( RetryRemoteSessionMgr.class ).in( Scopes.SINGLETON );
            } break;
            case InMemory: {
                binder.bind(RemoteAssetMgrProxy.class).to(InMemoryAssetMgrProxy.class).in(Scopes.SINGLETON);
                binder.bind(RemoteSearchMgrProxy.class).to(InMemorySearchMgrProxy.class).in(Scopes.SINGLETON);
                binder.bind( RemoteSessionMgrProxy.class ).to( InMemorySessionMgrProxy.class ).in( Scopes.SINGLETON );
            } break;
        }
    }
}
