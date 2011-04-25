/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.tracker.server;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Binder;
import java.util.Map;
import littleware.apps.tracker.Comment;
import littleware.apps.tracker.Member;
import littleware.apps.tracker.MemberAlias;
import littleware.apps.tracker.Product;
import littleware.apps.tracker.ProductAlias;
import littleware.apps.tracker.Queue;
import littleware.apps.tracker.Task;
import littleware.apps.tracker.TaskQueryManager;
import littleware.apps.tracker.Version;
import littleware.apps.tracker.VersionAlias;
import littleware.asset.AssetType;
import littleware.asset.server.AssetSpecializer;
import littleware.asset.server.NullAssetSpecializer;
import littleware.asset.server.bootstrap.AbstractServerModule;
import littleware.asset.server.bootstrap.ServerBootstrap;
import littleware.asset.server.bootstrap.ServerBootstrap.ServerProfile;
import littleware.asset.server.bootstrap.ServerModule;
import littleware.asset.server.bootstrap.ServerModuleFactory;

/**
 * Bind server-side TaskQueryManager implementation, etc.
 */
public class TrackerServerModule extends AbstractServerModule {
    
    private static final Map<AssetType, Class<? extends AssetSpecializer>> typeMap;

    static {
        final ImmutableMap.Builder<AssetType, Class<? extends AssetSpecializer>> builder =
                ImmutableMap.builder();
        builder.put( Queue.QUEUE_TYPE, NullAssetSpecializer.class
                ).put( Comment.COMMENT_TYPE, NullAssetSpecializer.class
                ).put( Product.PRODUCT_TYPE, SimpleProductSpecializer.class
                ).put( ProductAlias.PA_TYPE, SimpleProductSpecializer.class
                ).put( Version.VERSION_TYPE, SimpleProductSpecializer.class
                ).put( VersionAlias.VA_TYPE, SimpleProductSpecializer.class
                ).put( Member.MEMBER_TYPE, SimpleProductSpecializer.class
                ).put( MemberAlias.MA_TYPE, SimpleProductSpecializer.class );
        typeMap = builder.put( Task.TASK_TYPE, SimpleTaskSpecializer.class ).build();

        /*
                ).add( Product.PRODUCT_TYPE
                ).add( ProductAlias.PA_TYPE
                ).add( Version.VERSION_TYPE
                ).add( VersionAlias.VA_TYPE
                ).add( Member.MEMBER_TYPE
                ).add( MemberAlias.MA_TYPE
         */
    }


    public static class Factory implements ServerModuleFactory {

        @Override
        public ServerModule build( ServerProfile profile ) {
            return new TrackerServerModule( profile );
        }

    }

    //----------------------------

    private TrackerServerModule( ServerBootstrap.ServerProfile profile ) {
        super( profile, typeMap, emptyServerListeners );
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(TaskQueryManager.class).to(JpaTaskQueryManager.class);
    }
}
