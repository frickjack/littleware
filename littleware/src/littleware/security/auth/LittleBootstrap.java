/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.security.auth;

import com.google.inject.ImplementedBy;

/**
 * Just a little interface that a bootstrap class
 * should implement.  Start out using this on the
 * server side.
 */
@ImplementedBy(GuiceOSGiBootstrap.class)
public interface LittleBootstrap {
    /**
     * Bootstrap a littleware component on the client or server.
     * Returns after activating the littleware bundles.
     * The littleware engine might still require some asynchronous
     * startup time before access upon return depending
     * on the underlying implementation.
     */
    public void bootstrap ();

    /**
     * Shutdown the littleware component associated with this object.
     */
    public void shutdown();
}
