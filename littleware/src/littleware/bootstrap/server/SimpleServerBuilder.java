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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import littleware.bootstrap.server.ServerBootstrap.ServerBuilder;
import littleware.bootstrap.server.ServerBootstrap.ServerProfile;
import littleware.bootstrap.server.ServerModule.ServerFactory;

/**
 *
 * @author pasquini
 */
public class SimpleServerBuilder implements ServerBootstrap.ServerBuilder {
    private final List<ServerFactory>  moduleList = new ArrayList<ServerFactory>();

    @Override
    public Collection<ServerFactory> getModuleList() {
        return ImmutableList.copyOf( moduleList );
    }

    @Override
    public ServerBuilder addModuleFactory(ServerFactory factory) {
        moduleList.add( factory );
        return this;
    }

    @Override
    public ServerBuilder removeModuleFactory(ServerFactory factory) {
        moduleList.remove(factory);
        return this;
    }

    @Override
    public ServerBuilder config(ServerProfile config) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ServerBootstrap build() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
