/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.client.bootstrap;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Collections;
import littleware.asset.AssetType;
import littleware.asset.client.LittleServiceListener;
import littleware.bootstrap.AppBootstrap;
import littleware.bootstrap.helper.AbstractAppModule;
import littleware.security.auth.ServiceType;

public abstract class AbstractClientModule extends AbstractAppModule implements ClientModule {
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
        super( profile );
        this.assetTypes = ImmutableList.copyOf( assetTypes );
        this.serviceTypes = ImmutableList.copyOf( serviceTypes );
        this.serviceListeners = ImmutableList.copyOf( serviceListeners );
    }

    /**
     * Constructor with empty type-set, server-set, and listener list
     */
     public AbstractClientModule( AppBootstrap.AppProfile profile ) {
         this( profile, emptyAssetTypes, emptyServiceTypes, emptyServiceListeners );
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

}
