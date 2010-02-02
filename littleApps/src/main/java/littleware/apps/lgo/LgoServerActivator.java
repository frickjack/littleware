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

import com.google.inject.Inject;
import littleware.base.Maybe;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 *
 * @author pasquini
 */
public class LgoServerActivator implements BundleActivator {

    private final LgoServer.ServerBuilder serverBuilder;
    private Maybe<LgoServer> maybeServer = Maybe.empty();

    @Inject
    public LgoServerActivator(LgoServer.ServerBuilder serverBuilder) {
        this.serverBuilder = serverBuilder;
    }

    @Override
    public void start(BundleContext bc) throws Exception {
        maybeServer = Maybe.something(serverBuilder.launch());
    }

    @Override
    public void stop(BundleContext bc) throws Exception {
        if (maybeServer.isSet()) {
            try {
                maybeServer.get().shutdown();
            } finally {
                maybeServer = maybeServer.empty();
            }
        }
    }
}
