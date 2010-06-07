/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.lgo;

import com.google.inject.ImplementedBy;

/**
 * Interface for configuring, launching,
 * collecting stats (eventually), and shutting down LgoServer
 */
public interface LgoServer {
    @ImplementedBy(JettyServerBuilder.class)
    public interface ServerBuilder {
        public LgoServer launch ();
    }

    /**
     * Server is either running or shutdown
     *
     * @return true if server is shutdown
     */
    public boolean isShutdown();

    /**
     * NOOP if server already shutdown
     */
    public void shutdown();
}
