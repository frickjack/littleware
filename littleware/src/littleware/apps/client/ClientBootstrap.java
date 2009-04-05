/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.client;

import com.google.inject.Binder;
import com.google.inject.Module;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;
import littleware.apps.lgo.EzModule;
import littleware.security.auth.AbstractGOBootstrap;
import littleware.security.auth.LittleBootstrap;
import org.osgi.framework.BundleActivator;

/**
 * Client side OSGi bootstrap with Guice injection -
 * very similar to server side implementation.
 */
public class ClientBootstrap extends AbstractGOBootstrap {

    private static final Logger olog = Logger.getLogger(ClientBootstrap.class.getName());

    public ClientBootstrap() {
        super(
                Arrays.asList(
                new EzModule(),
                new littleware.apps.swingclient.StandardSwingGuice(),
                new littleware.apps.client.StandardClientGuice(),
                new littleware.apps.misc.StandardMiscGuice(),
                new littleware.security.auth.ClientServiceGuice()),
                new ArrayList<Class<? extends BundleActivator>>(),
                false);
        this.getGuiceModule().add(
                new Module() {
            @Override
                    public void configure(Binder binder) {
                        binder.bind(LittleBootstrap.class).toInstance(ClientBootstrap.this);
                    }
                });
    }
}
