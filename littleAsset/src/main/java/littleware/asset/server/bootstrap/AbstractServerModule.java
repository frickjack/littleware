/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.bootstrap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import littleware.asset.AssetType;
import littleware.asset.server.AssetSpecializer;
import littleware.asset.server.LittleServerListener;
import littleware.bootstrap.AppBootstrap;
import littleware.bootstrap.helper.AbstractAppModule;
import littleware.bootstrap.helper.NullActivator;
import org.osgi.framework.BundleActivator;

public abstract class AbstractServerModule extends AbstractAppModule implements ServerModule {
    
    private final Map<AssetType, Class<? extends AssetSpecializer>> typeMap;
    private final Collection<Class<? extends LittleServerListener>> serverListeners;

    protected static final Map<AssetType, Class<? extends AssetSpecializer>> emptyTypeMap = Collections.emptyMap();
    protected static final Collection<Class<? extends LittleServerListener>> emptyServerListeners = Collections.emptyList();

    protected AbstractServerModule(AppBootstrap.AppProfile profile,
            Map<AssetType, Class<? extends AssetSpecializer>> typeMap,
            Collection<Class<? extends LittleServerListener>> serverListeners) {
        super( profile );
        this.typeMap = ImmutableMap.copyOf( typeMap );
        this.serverListeners = ImmutableList.copyOf( serverListeners );
    }

    protected AbstractServerModule( AppBootstrap.AppProfile profile ) {
        super( profile );
        this.typeMap = Collections.emptyMap();
        this.serverListeners = Collections.emptyList();
    }


    @Override
    public Map<AssetType, Class<? extends AssetSpecializer>> getAssetTypes() {
        return typeMap;
    }

    @Override
    public Collection<Class<? extends LittleServerListener>> getServerListeners() {
        return serverListeners;
    }

    @Override
    public Class<? extends BundleActivator> getActivator() {
        return NullActivator.class;
    }

}
