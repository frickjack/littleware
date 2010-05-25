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
import java.util.Collection;
import java.util.Collections;
import littleware.asset.AssetType;
import littleware.asset.client.LittleServiceListener;
import littleware.base.Maybe;
import littleware.bootstrap.client.AppBootstrap.AppProfile;
import littleware.security.auth.ServiceType;
import org.osgi.framework.BundleActivator;

public class AbstractClientModule implements ClientModule {
    private final AppProfile profile;
    private final Collection<AssetType>   assetTypes;
    private final Collection<ServiceType> serviceTypes;
    private final Collection<Class<? extends LittleServiceListener>> serviceListeners;

    public AbstractClientModule( AppBootstrap.AppProfile profile,
            Collection<AssetType> assetTypes,
            Collection<ServiceType> serviceTypes,
            Collection<Class<? extends LittleServiceListener>> serviceListeners
            ) {
        this.profile = profile;
        this.assetTypes = ImmutableList.copyOf( assetTypes );
        this.serviceTypes = ImmutableList.copyOf( serviceTypes );
        this.serviceListeners = ImmutableList.copyOf( serviceListeners );
    }

    @Override
    public Collection<AssetType> getAssetTypes() {
        return assetTypes;
    }

    @Override
    public Collection<ServiceType> getServiceTypes() {
        return serviceTypes;
    }

    @Override
    public Collection<Class<? extends LittleServiceListener>> getServiceListeners() {
        return serviceListeners;
    }

    @Override
    public AppProfile getProfile() {
        return profile;
    }

    @Override
    public Maybe<Class<? extends BundleActivator>> getActivator() {
        return Maybe.empty();
    }

    @Override
    public void configure(Binder binder) {
    }

}