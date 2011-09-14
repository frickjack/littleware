/*
 * Copyright 2011 http://code.google.com/p/littleware
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.server.bootstrap;

import littleware.bootstrap.AppBootstrap;

public interface ServerModuleFactory {

    /**
     * Build the server module configured for the given profile
     */
    public ServerModule buildServerModule( AppBootstrap.AppProfile profile );
}
