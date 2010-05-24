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
import littleware.bootstrap.client.AppBootstrap.AppBuilder;
import littleware.bootstrap.client.AppBootstrap.AppProfile;
import littleware.bootstrap.client.AppModule.AppFactory;

public class SimpleAppBuilder implements AppBootstrap.AppBuilder {
    private final List<AppFactory>  factoryList = new ArrayList<AppFactory>();
    private AppProfile profile;

    @Override
    public Collection<AppFactory> getModuleList() {
        return ImmutableList.copyOf(factoryList);
    }

    @Override
    public AppBuilder addModuleFactory(AppFactory factory) {
        factoryList.add(factory);
        return this;
    }

    @Override
    public AppBuilder removeModuleFactory(AppFactory factory) {
        factoryList.remove(factory);
        return this;
    }

    @Override
    public AppBuilder config(AppProfile value) {
        this.profile = value;
        return this;
    }

    @Override
    public AppBootstrap build() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
