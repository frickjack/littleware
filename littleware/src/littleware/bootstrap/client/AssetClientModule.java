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
import java.io.IOException;
import java.util.Collection;
import littleware.asset.AssetType;
import littleware.asset.client.ClientSessionActivator;
import littleware.asset.client.LittleServiceListener;
import littleware.asset.client.SimpleClientCache;
import littleware.base.AssertionFailedException;
import littleware.base.Maybe;
import littleware.base.PropertiesGuice;
import littleware.bootstrap.client.AppBootstrap.AppProfile;
import littleware.security.SecurityAssetType;
import littleware.security.auth.ServiceType;

public class AssetClientModule extends AbstractClientModule {

    public static class Factory implements ClientModule.ClientFactory {

        private final Collection<AssetType> typeSet;

        {
            final ImmutableList.Builder<AssetType> builder = ImmutableList.builder();
            typeSet = builder.add(AssetType.GENERIC).add(AssetType.HOME).add(AssetType.LINK).add(AssetType.LOCK
                    ).add(SecurityAssetType.ACL
                    ).add(SecurityAssetType.ACL_ENTRY
                    ).add(SecurityAssetType.GROUP
                    ).add( SecurityAssetType.GROUP_MEMBER
                    ).add( SecurityAssetType.PRINCIPAL
                    ).add( SecurityAssetType.QUOTA
                    ).add( SecurityAssetType.SERVICE_STUB
                    ).add( SecurityAssetType.SESSION
                    ).add( SecurityAssetType.USER
                    ).build();
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
            serviceSet = builder.add( ServiceType.ACCOUNT_MANAGER
                    ).add( ServiceType.ASSET_MANAGER
                    ).add( ServiceType.ASSET_SEARCH
                    ).build();
        }

        @Override
        public ClientModule build(AppProfile profile) {
            return new AssetClientModule( profile, typeSet, serviceSet, listenerSet );
        }
    }

    private AssetClientModule(AppBootstrap.AppProfile profile,
            Collection<AssetType> assetTypes,
            Collection<ServiceType> serviceTypes,
            Collection<Class<? extends LittleServiceListener>> serviceListeners) {
        super(profile, assetTypes, serviceTypes, serviceListeners);
    }

    @Override
    public void configure( Binder binder ) {
        try {
            (new PropertiesGuice()).configure(binder);
        } catch (IOException ex) {
            throw new AssertionFailedException( "Failed to load littleware.properties", ex );
        }
    }

    @Override
    public Maybe<Class<ClientSessionActivator>> getActivator() {
        return Maybe.something( ClientSessionActivator.class );
    }
}
