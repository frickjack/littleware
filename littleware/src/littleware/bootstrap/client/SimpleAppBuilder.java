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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Logger;
import littleware.bootstrap.AbstractLittleBootstrap;
import littleware.bootstrap.client.AppBootstrap.AppBuilder;
import littleware.bootstrap.client.AppBootstrap.AppProfile;
import littleware.bootstrap.client.AppModuleFactory;

public class SimpleAppBuilder implements AppBootstrap.AppBuilder {
    private static final Logger     log = Logger.getLogger( SimpleAppBuilder.class.getName() );

    private final List<AppModuleFactory>  factoryList = new ArrayList<AppModuleFactory>();
    private AppProfile profile;

    {
        for( AppModuleFactory moduleFactory : ServiceLoader.load( AppModuleFactory.class ) ) {
            factoryList.add( moduleFactory );
        }
    }

    @Override
    public Collection<AppModuleFactory> getModuleSet() {
        return ImmutableList.copyOf(factoryList);
    }

    @Override
    public AppBuilder addModuleFactory(AppModuleFactory factory) {
        factoryList.add(factory);
        return this;
    }

    @Override
    public AppBuilder removeModuleFactory(AppModuleFactory factory) {
        factoryList.remove(factory);
        return this;
    }

    @Override
    public AppBuilder profile(AppProfile value) {
        this.profile = value;
        return this;
    }

    private static class Bootstrap extends AbstractLittleBootstrap<AppModule> implements AppBootstrap {
        private final AppProfile profile;
        public Bootstrap( Collection<? extends AppModule> moduleSet, AppBootstrap.AppProfile profile ) {
            super( moduleSet );
            this.profile = profile;
        }

        @Override
        public AppBootstrap.AppProfile getProfile() { return profile; }
    }

    @Override
    public AppBootstrap build() {
        final ImmutableList.Builder<AppModule> builder = ImmutableList.builder();
        for ( AppModuleFactory factory : factoryList ) {
            builder.add( factory.build( profile ) );
        }
        return new Bootstrap( builder.build(), profile );
    }

}
