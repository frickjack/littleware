/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.bootstrap.server;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Binder;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import littleware.asset.AssetType;
import littleware.asset.server.AssetSpecializer;
import littleware.asset.server.LittleServerListener;
import littleware.base.Maybe;
import littleware.bootstrap.server.ServerBootstrap.ServerProfile;
import littleware.security.auth.ServiceType;
import littleware.security.auth.server.ServiceProviderFactory;
import org.osgi.framework.BundleActivator;

public class AbstractServerModule implements ServerModule {

    private final ServerProfile profile;
    private final Map<AssetType, Class<? extends AssetSpecializer>> typeMap;
    private final Map<ServiceType, Class<? extends ServiceProviderFactory>> serviceMap;
    private final Collection<Class<? extends LittleServerListener>> serverListeners;

    protected AbstractServerModule(ServerBootstrap.ServerProfile profile,
            Map<AssetType, Class<? extends AssetSpecializer>> typeMap,
            Map<ServiceType, Class<? extends ServiceProviderFactory>> serviceMap,
            Collection<Class<? extends LittleServerListener>> serverListeners) {
        this.profile = profile;
        this.typeMap = ImmutableMap.copyOf( typeMap );
        this.serviceMap = ImmutableMap.copyOf( serviceMap );
        this.serverListeners = ImmutableList.copyOf( serverListeners );
    }

    protected AbstractServerModule( ServerBootstrap.ServerProfile profile ) {
        this.profile = profile;
        this.typeMap = Collections.emptyMap();
        this.serviceMap = Collections.emptyMap();
        this.serverListeners = Collections.emptyList();
    }

    @Override
    public ServerProfile getProfile() {
        return profile;
    }

    @Override
    public Map<AssetType, Class<? extends AssetSpecializer>> getAssetTypes() {
        return typeMap;
    }

    @Override
    public Map<ServiceType, Class<? extends ServiceProviderFactory>> getServiceTypes() {
        return serviceMap;
    }

    @Override
    public Collection<Class<? extends LittleServerListener>> getServerListeners() {
        return serverListeners;
    }

    @Override
    public Maybe<Class<? extends BundleActivator>> getActivator() {
        return Maybe.empty();
    }

    @Override
    public void configure(Binder binder) {
    }
}
