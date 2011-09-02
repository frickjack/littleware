/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.tracker.internal;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import littleware.apps.tracker.Comment;
import littleware.apps.tracker.Member;
import littleware.apps.tracker.MemberAlias;
import littleware.apps.tracker.MemberIndex;
import littleware.apps.tracker.Product;
import littleware.apps.tracker.ProductAlias;
import littleware.apps.tracker.Queue;
import littleware.apps.tracker.Task;
import littleware.apps.tracker.Version;
import littleware.apps.tracker.VersionAlias;
import littleware.apps.tracker.ZipUtil;
import littleware.asset.spi.AssetProviderRegistry;
import littleware.bootstrap.AppBootstrap.AppProfile;
import littleware.bootstrap.AppModule;
import littleware.bootstrap.AppModuleFactory;
import littleware.bootstrap.helper.NullActivator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Application-mode module just binds implementations
 * for ZipUtil and other utility classes.
 */
public class LittleTrackerModule implements AppModule {

    private final AppProfile profile;

    public static class AppFactory implements AppModuleFactory {

        @Override
        public AppModule build(AppProfile profile) {
            return new LittleTrackerModule(profile);
        }
    }

    public LittleTrackerModule(AppProfile profile) {
        this.profile = profile;
    }

    @Override
    public AppProfile getProfile() {
        return profile;
    }

    public static class Activator implements BundleActivator {

        @Inject
        public Activator(AssetProviderRegistry assetRegistry,
                Provider<Comment.CommentBuilder> commentProvider,
                Provider<Queue.QueueBuilder> queueProvider,
                Provider<Task.TaskBuilder> taskProvider,
                Provider<Product.ProductBuilder> productProvider,
                Provider<ProductAlias.PABuilder> paProvider,
                Provider<Version.VersionBuilder> versionProvider,
                Provider<VersionAlias.VABuilder> vaProvider,
                Provider<Member.MemberBuilder> memberProvider,
                Provider<MemberAlias.MABuilder> maProvider) {
            assetRegistry.registerService( Comment.COMMENT_TYPE, commentProvider );
            assetRegistry.registerService( Queue.QUEUE_TYPE, queueProvider );
            assetRegistry.registerService( Task.TASK_TYPE, taskProvider );
            assetRegistry.registerService( Product.PRODUCT_TYPE, productProvider );
            assetRegistry.registerService( ProductAlias.PA_TYPE, paProvider );
            assetRegistry.registerService( Version.VERSION_TYPE, versionProvider );
            assetRegistry.registerService( VersionAlias.VA_TYPE, vaProvider );
            assetRegistry.registerService( Member.MEMBER_TYPE, memberProvider );
            assetRegistry.registerService( MemberAlias.MA_TYPE, maProvider );
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
        binder.bind(ZipUtil.class).to(SimpleZipUtil.class).in(Scopes.SINGLETON);
        binder.bind(MemberIndex.IndexBuilder.class).to(SimpleIndexBuilder.class);
        binder.bind(Task.TaskBuilder.class).to(SimpleTaskBuilder.class);
        binder.bind(Queue.QueueBuilder.class).to(SimpleQueueBuilder.class);
        binder.bind(Comment.CommentBuilder.class).to(SimpleCommentBuilder.class);
        binder.bind(Product.ProductBuilder.class).to(SimpleProductBuilder.class);
        binder.bind(ProductAlias.PABuilder.class).to(SimplePABuilder.class);
        binder.bind(Version.VersionBuilder.class).to(SimpleVersionBuilder.class);
        binder.bind(VersionAlias.VABuilder.class).to(SimpleVABuilder.class);
        binder.bind(Member.MemberBuilder.class).to(SimpleMemberBuilder.class);
        binder.bind(MemberAlias.MABuilder.class).to(SimpleMABuilder.class);
    }
}
