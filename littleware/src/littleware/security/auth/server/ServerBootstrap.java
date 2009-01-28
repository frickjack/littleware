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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.apps.addressbook.server.AddressServerActivator;
import littleware.apps.filebucket.server.BucketServerActivator;
import littleware.apps.filebucket.server.BucketServerGuice;
import littleware.apps.tracker.server.TrackerServerActivator;
import littleware.asset.server.AssetServerGuice;
import littleware.base.AssertionFailedException;
import littleware.base.PropertiesGuice;
import littleware.base.PropertiesLoader;
import littleware.db.DbGuice;
import littleware.security.auth.LittleBootstrap;
import littleware.security.auth.SessionManager;
import littleware.security.auth.SessionUtil;
import littleware.security.server.SecurityServerActivator;
import littleware.security.server.SecurityServerGuice;
import org.apache.felix.framework.Felix;
import org.apache.felix.framework.util.FelixConstants;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleException;

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
public class ServerBootstrap implements LittleBootstrap {
    private static final Logger olog = Logger.getLogger( ServerBootstrap.class.getName() );

    private final List<Module>  ov_guice;
    {
        try {
            ov_guice = new ArrayList(
                    Arrays.asList(
                    new PropertiesGuice( PropertiesLoader.get().loadProperties() ),
                    new AssetServerGuice (),
                    new AuthServerGuice(),
                    new BucketServerGuice(),
                    new SecurityServerGuice (),
                    new DbGuice()
                    )
                );
        } catch ( IOException ex ) {
            throw new AssertionFailedException( "Failed default Guice setup", ex );
        }
    }
    private final List<Class<? extends BundleActivator>>  ov_osgi = new ArrayList(
            Arrays.asList(
                ServerActivator.class,
                SecurityServerActivator.class,
                TrackerServerActivator.class,
                AddressServerActivator.class,
                BucketServerActivator.class
            ));


    /**
     * Guice module list with which bootstrapServer
     * builds its injector.  The module list is dervied
     * from the lw.guice_module property in littleware.properties if
     * set - otherwise a default set of modules is used.
     *
     * @return list of allocated modules that the caller may modify
     *        to change bootstrap behavior
     */
    public List<Module>  getGuiceModule() {
        return ov_guice;
    }

    /**
     * OSGi activators to be allocated via the Guice injector,
     * and then passed on to OSGi for bootstrap.
     *
     * @return list of OSGi activator classes that the caller may
     *              modify to change bootstrap behavior
     */
    public List<Class<? extends BundleActivator>> getOSGiActivator() {
        return ov_osgi;
    }

    private boolean   ob_bootstrap = false;
    private Felix     ofelix = null;


    /**
     * Setup the GUICE injector, launch the registered
     * OSGi bundle activators, and inject the SessionManager into
     * the SessionUtil singleton.
     *
     * @exception IllegalStateException if bootstrap has already run
     * @exception IOException on failure to load init daa
     */
    public void bootstrap () {
        if ( ob_bootstrap ) {
            throw new IllegalStateException ( "Bootstrap already run" );
        }
        Properties prop_littleware = null;
        try {
            prop_littleware = PropertiesLoader.get().loadProperties();
            String s_modules = prop_littleware.getProperty( "lw.guice_module", "");
            if ( s_modules.length () > 0 ) {
                olog.log( Level.INFO, "Loading custom guice modules: " + s_modules );
                for ( String s_mod : s_modules.split( "[:,; ]+" ) ) {
                    try {
                        ov_guice.add (
                                (Module) Class.forName(s_mod).newInstance()
                                );
                    } catch ( Exception ex ) {
                        olog.log( Level.SEVERE, "Failed to load Guice module: " + s_mod, ex );
                        throw new AssertionFailedException( "Failed to load Guice module: " + s_mod, ex );
                    }
                }
            }
        } catch ( IOException ex ) {
            throw new AssertionFailedException( "Unable to load littleware.properties", ex );
        }

        Injector injector = Guice.createInjector(
                ov_guice
                );

        // Inject the local SessionManager for clients accessing SessionUtil
        final SessionManager mgr_session = injector.getInstance( SessionManager.class );
        injector.injectMembers( SessionUtil.get() );
        try {
            if ( mgr_session != SessionUtil.get().getSessionManager() ) {
                throw new AssertionFailedException( "What the frick!" );
            }
        } catch ( Exception ex ) {
            throw new AssertionFailedException( "What the frick2!", ex );
        }

        // Get Guice injected instances of the OSGi BundleActivators,
        // and bootstrap OSGi
        Map<String,Object> map_felix = new HashMap<String,Object>();
        List<BundleActivator> v_activate = new ArrayList<BundleActivator> ();
        for( Class<? extends BundleActivator> class_act : ov_osgi ) {
            v_activate.add( injector.getInstance( class_act ) );
        }
        map_felix.put( FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, v_activate );
        ofelix = new Felix( map_felix );
        try {
            ofelix.start();
        } catch (BundleException ex) {
            throw new AssertionFailedException( "Failed to bootstrap Felix OSGi", ex );
        }
        
        ob_bootstrap = true;
    }

    public void shutdown() {
        try {
            ofelix.stop();
            ofelix.waitForStop( 5000 );
        } catch ( RuntimeException ex ) {
            throw ex;
        } catch ( Exception ex ) {
            olog.log( Level.WARNING, "Felix shutdown caught exception", ex );
        }

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
