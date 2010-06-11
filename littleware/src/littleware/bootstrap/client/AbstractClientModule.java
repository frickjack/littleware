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
import littleware.bootstrap.NullActivator;
import littleware.bootstrap.client.AppBootstrap.AppProfile;
import littleware.security.auth.ServiceType;
import org.osgi.framework.BundleActivator;

public class AbstractClientModule implements ClientModule {
    private final AppProfile profile;
    private final Collection<AssetType>   assetTypes;
    private final Collection<ServiceType> serviceTypes;
    private final Collection<Class<? extends LittleServiceListener>> serviceListeners;

    protected static final Collection<AssetType>   emptyAssetTypes = Collections.emptyList();
    protected static final Collection<ServiceType> emptyServiceTypes = Collections.emptyList();
    protected static final Collection<Class<? extends LittleServiceListener>> emptyServiceListeners = Collections.emptyList();

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

    /**
     * Constructor with empty type-set, server-set, and listener list
     */
     public AbstractClientModule( AppBootstrap.AppProfile profile ) {
         this.profile = profile;
         this.assetTypes = Collections.emptyList();
         this.serviceTypes = Collections.emptyList();
         this.serviceListeners = Collections.emptyList();
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
    public Class<? extends BundleActivator> getActivator() {
        return NullActivator.class;
    }

    @Override
    public void configure(Binder binder) {
    }

}
