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
    public AbstractServerModule( ServerBootstrap.ServerProfile profile ) {
        this.profile = profile;
    }
    @Override
    public ServerProfile getProfile() {
        return profile;
    }

    @Override
    public Map<AssetType, Class<? extends AssetSpecializer>> getAssetTypes() {
        return Collections.emptyMap();
    }

    @Override
    public Map<ServiceType, Class<? extends ServiceProviderFactory>> getServiceTypes() {
        return Collections.emptyMap();
    }

    @Override
    public Collection<Class<? extends LittleServerListener>> getServerListeners() {
        return Collections.emptyList();
    }

    @Override
    public Maybe<Class<? extends BundleActivator>> getActivator() {
        return Maybe.empty();
    }

    @Override
    public void configure(Binder binder) {
    }

}
