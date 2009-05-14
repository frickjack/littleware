/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.security.auth.server;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.apps.addressbook.server.AddressServerActivator;
import littleware.apps.filebucket.server.BucketServerActivator;
import littleware.apps.filebucket.server.BucketServerGuice;
import littleware.apps.tracker.server.TrackerServerActivator;
import littleware.asset.server.AssetServerGuice;
import littleware.asset.server.db.jpa.HibernateGuice;
import littleware.asset.server.db.jpa.J2EEGuice;
import littleware.asset.server.db.postgres.PostgresGuice;
import littleware.db.DbGuice;
import littleware.security.auth.AbstractGOBootstrap;
import littleware.security.auth.LittleBootstrap;
import littleware.security.server.SecurityServerActivator;
import littleware.security.server.SecurityServerGuice;

/**
 * Singleton class bootstraps littleware server.
 * First loads lw.guice_modules and lw.osgi_bundles properties
 * from littleware.properties.  Next builds a Guice injector
 * with the specified modules.  Finally, launches the OSGi
 * bundle activators within an embedded OSGi environment.
 *
 * Note: server environment differs from client environment.
 * A global shared AssetManager, AssetSearchManager, etc. rely
 * on the underlying RMI runtime to setup a proper JAAS
 * environment from which the user associated with some
 * operation may be derived.  It is up to the server implementation
 * to enforce security constraints.  The injected search and asset
 * managers take care of this for many purposes.
 *
 * On the client each manager
 * is associated with the client session associated with
 * the SessionHelper from which the manager is derived.
 */
public class ServerBootstrap extends AbstractGOBootstrap {
    private static final Logger olog = Logger.getLogger( ServerBootstrap.class.getName() );

    public ServerBootstrap() {
        super(
                  Arrays.asList(
                  //new PostgresGuice(),
                  //new HibernateGuice(),
                  new J2EEGuice(),
                    new AssetServerGuice (),
                    new AuthServerGuice(),
                    new BucketServerGuice(),
                    new SecurityServerGuice (),
                    new DbGuice()
                    ),
            Arrays.asList(
                ServerActivator.class,
                SecurityServerActivator.class,
                TrackerServerActivator.class,
                AddressServerActivator.class,
                BucketServerActivator.class
            ),
            true
            );
    }


    public static void main ( String[] v_argv ) {
        olog.log( Level.INFO, "Testing OSGi bootstrap" );
        LittleBootstrap boot = new ServerBootstrap ();
        boot.bootstrap();
        olog.log( Level.INFO, "Sleeping 10 seconds before shutdown" );
        try {
            Thread.sleep(10000);
            boot.shutdown();
            olog.log( Level.INFO, "Shutdown issued, sleep 5 seconds before exit" );
            Thread.sleep( 5000 );
        } catch (InterruptedException ex) {
            olog.log(Level.SEVERE, null, ex);
        }
        System.exit( 0 );
    }
}
