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

import java.util.Collection;
import littleware.bootstrap.LittleBootstrap;

/**
 * Server-side bootstrap configuration manager
 */
public interface ServerBootstrap extends LittleBootstrap {

    public enum ServerProfile {
        Standalone, J2EE;
    }

    public interface ServerBuilder extends LittleBootstrap.Builder {

        /**
         * List of littleware modules registered with this bootstrap.
         */
        public Collection<ServerModule.ServerFactory> getModuleList();

        public ServerBuilder addModuleFactory(ServerModule.ServerFactory factory);

        public ServerBuilder removeModuleFactory(ServerModule.ServerFactory factory);

        public ServerBuilder config( ServerProfile config );

        @Override
        public ServerBootstrap build();
    }
}

