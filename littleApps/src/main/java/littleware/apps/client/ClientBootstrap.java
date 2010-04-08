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
import java.util.Arrays;
import java.util.logging.Logger;
import littleware.apps.lgo.LgoActivator;
import littleware.apps.lgo.LgoGuice;
import littleware.asset.client.ClientSessionActivator;
import littleware.base.Maybe;
import littleware.security.auth.ClientServiceGuice;
import littleware.security.auth.LittleBootstrap;
import littleware.security.auth.client.CacheActivator;
//import org.apache.jcs.JCS;
//import org.apache.jcs.access.exception.CacheException;
//import org.apache.jcs.engine.control.CompositeCacheManager;

/**
 * Client side OSGi bootstrap with Guice injection -
 * very similar to server side implementation.
 */
public class ClientBootstrap extends NullBootstrap {

    private static final Logger log = Logger.getLogger(ClientBootstrap.class.getName());
    private final ClientServiceGuice clientGuice;

    /**
     * Property specifies the server host to authenticate to
     * if authentication is necessary.  Actually accesses
     * ClientServiceGuice host property.
     */
    public Maybe<String> getHost() {
        return clientGuice.getHost();
    }

    public void setHost(Maybe<String> host) {
        clientGuice.setHost(host);
    }

    /**
     * Setup bootstrap with preconfigured ClientServiceGuice module.
     */
    public ClientBootstrap(ClientServiceGuice clientGuice) {
        for (Module guice : Arrays.asList(
                new LgoGuice(),
                new littleware.apps.swingclient.StandardSwingGuice(),
                new littleware.apps.client.StandardClientGuice(),
                new littleware.apps.misc.StandardMiscGuice(),
                clientGuice
                )) {
            this.getGuiceModule().add(guice);
        }

        this.getOSGiActivator().add(AssetModelServiceListener.class);
        this.getOSGiActivator().add(CacheActivator.class);
        this.getOSGiActivator().add(SyncWithServer.class);
        this.getOSGiActivator().add(LgoActivator.class);
        this.getOSGiActivator().add(ClientSessionActivator.class);

        this.clientGuice = clientGuice;
    }

    /**
     * Allow override of default remote server host
     *
     * @param sHost to connect to
     */
    public ClientBootstrap(String sHost) {
        this(new ClientServiceGuice(sHost));
    }

    public ClientBootstrap() {
        this(new ClientServiceGuice());
    }
}
