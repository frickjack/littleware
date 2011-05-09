/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.asset.internal;

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
import littleware.asset.client.internal.RetryRemoteAstMgr;
import littleware.asset.client.internal.RetryRemoteSearchMgr;
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

    //-------------------------------------

    public LittleAssetModule( AppProfile profile ) {
        super( profile );
    }


    public static class Activator implements BundleActivator {

        @Inject
        public Activator( AssetProviderRegistry assetRegistry,
                Provider<TreeNode.TreeNodeBuilder> nodeProvider,
                Provider<GenericAsset.GenericBuilder> genericProvider,
                Provider<LinkAsset.LinkBuilder> linkProvider,
                Provider<LittleHome.HomeBuilder> homeProvider
                ) {
            assetRegistry.registerService(LittleHome.HOME_TYPE, homeProvider);
            assetRegistry.registerService( TreeNode.TREE_NODE_TYPE, nodeProvider );
            assetRegistry.registerService( GenericAsset.GENERIC, genericProvider );
            assetRegistry.registerService( LinkAsset.LINK_TYPE, linkProvider);
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
        binder.bind( LittleHome.HomeBuilder.class ).to( LittleHomeBuilder.class );
        binder.bind( GenericAsset.GenericBuilder.class ).to( SimpleGenericBuilder.class );
        binder.bind( TreeNode.TreeNodeBuilder.class ).to( SimpleTreeNodeBuilder.class );
        binder.bind( LinkAsset.LinkBuilder.class ).to( SimpleLinkBuilder.class );
        binder.bind( IdWithClock.Builder.class ).to( IdWithClockBuilder.class );
        binder.bind(AssetPathFactory.class).to(SimpleAssetPathFactory.class);
        binder.bind( AssetTreeTemplate.TemplateBuilder.class ).to( SimpleTemplateBuilder.class ).in( Scopes.SINGLETON );
        binder.bind( HumanPicklerProvider.class ).to( SimpleHumanRegistry.class ).in( Scopes.SINGLETON );
        binder.bind( XmlPicklerProvider.class ).to( SimpleXmlRegistry.class ).in( Scopes.SINGLETON );
        binder.bind( AssetProviderRegistry.class ).to( SimpleAssetRegistry.class ).in( Scopes.SINGLETON );

        // Avoid binding RemoteManager - gets bound in the server environment too
        //binder.bind( RemoteAssetManager.class ).
        binder.bind( RetryRemoteAstMgr.class ).in( Scopes.SINGLETON );
        //binder.bind( RemoteSearchManager.class ).
        binder.bind( RetryRemoteSearchMgr.class ).in( Scopes.SINGLETON );
    }

}
