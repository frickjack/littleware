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

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.base.AssertionFailedException;
import littleware.base.PropertiesGuice;
import littleware.base.PropertiesLoader;
import org.apache.felix.framework.Felix;
import org.apache.felix.framework.cache.BundleCache;
import org.apache.felix.framework.util.FelixConstants;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleException;

/**
 * Base implementation of GuiceOSGiBootstrap that
 * subtypes specialize with default Guice modules and
 * OSGi BundleActivators to bring up clients and
 * servers in different configurations.
 * The guice module list starts out with a PropertiesGuice
 * instance loading littleware.properties,
 * and the bootstrap loads additional guice modules from
 * the lw.guice_module property or lw_client.guice_module property
 * depending on whether the environment is server or client.
 * Similarly loads
 * additional OSGi bundle classes from the lw.osgi_bundle property
 * or lw_client.osgi_bundle property.
 * The separate server/client properties are necessary as it's possible
 * for a client and server to coexist in the same VM.
 */
public abstract class AbstractGOBootstrap implements GuiceOSGiBootstrap {
    private static final Logger olog = Logger.getLogger( AbstractGOBootstrap.class.getName() );

    private final List<Module>  ovGuice = new ArrayList<Module>();
    {
        try {
            ovGuice.add(
                    new PropertiesGuice( PropertiesLoader.get().loadProperties() )
                    );
            ovGuice.add(
                    new Module() {
                @Override
                public void configure(Binder binder) {
                    binder.bind( GuiceOSGiBootstrap.class ).toInstance( AbstractGOBootstrap.this );
                }
            }
                    );
        } catch ( IOException ex ) {
            throw new AssertionFailedException( "Failed default Guice setup", ex );
        }
    }

    private final List<Class<? extends BundleActivator>>  ovOSGi = new ArrayList<Class<? extends BundleActivator>>();
    private final boolean obServer;
    
    /**
     * Inject bootstrap environment
     * 
     * @param vAddGuice guice modules to add to littleware.properties setup
     * @param vAddOSGi OSGi bundles to boot
     * @param bServer true to indicate server bootstrap, false indicates client -
     *                few small conditional code blocks
     */
    protected AbstractGOBootstrap( List<Module> vAddGuice, List<Class<? extends BundleActivator>> vAddOSGi,
            boolean bServer ) {
        ovGuice.addAll( vAddGuice );
        ovOSGi.addAll( vAddOSGi );
        obServer = bServer;
    }

    /**
     * Guice module list with which bootstrapServer
     * builds its injector.  The module list is dervied
     * from the lw.guice_module property in littleware.properties if
     * set - otherwise a default set of modules is used.
     *
     * @return list of allocated modules that the caller may modify
     *        to change bootstrap behavior
     */
    @Override
    public List<Module>  getGuiceModule() {
        return ovGuice;
    }

    /**
     * OSGi activators to be allocated via the Guice injector,
     * and then passed on to OSGi for bootstrap.
     *
     * @return list of OSGi activator classes that the caller may
     *              modify to change bootstrap behavior
     */
    @Override
    public List<Class<? extends BundleActivator>> getOSGiActivator() {
        return ovOSGi;
    }

    private boolean   ob_bootstrap = false;
    private Felix     ofelix = null;

    /**
     * Internal method loads extra modules and bundles
     * from the lw/lw_client littleware properties.
     */
    protected void addInfoFromProperties () {
        try {
            /**
             * Load extensions from property file.
             * It's possible for client and server to exist
             * in same VM, so may need to bootstrap separate modules.
             */
            final String   sGuiceName = obServer ? "lw.guice_module" : "lw_client.guice_module";
            final String   sBundleName = obServer ? "lw.osgi_bundle" : "lw_client.osgi_bundle";

            final Properties propLittleware = PropertiesLoader.get().loadProperties();
            final String sModules = propLittleware.getProperty( sGuiceName, "");
            if ( sModules.length () > 0 ) {
                olog.log( Level.INFO, "Loading custom guice modules: " + sModules );
                for ( String s_mod : sModules.split( "[:,; ]+" ) ) {
                    try {
                        ovGuice.add (
                                (Module) Class.forName(s_mod).newInstance()
                                );
                    } catch ( Exception ex ) {
                        olog.log( Level.SEVERE, "Failed to load Guice module: " + s_mod, ex );
                        throw new AssertionFailedException( "Failed to load Guice module: " + s_mod, ex );
                    }
                }
            }
            final String sBundles = propLittleware.getProperty( sBundleName, "");
            if ( sBundles.length () > 0 ) {
                olog.log( Level.INFO, "Loading custom OSGi bundles: " + sBundles );
                for ( String sBundle : sBundles.split( "[:,; ]+" ) ) {
                    try {
                        ovOSGi.add (
                                (Class<? extends BundleActivator>) Class.forName(sBundle)
                                );
                    } catch ( Exception ex ) {
                        olog.log( Level.SEVERE, "Failed to load OSGi bundle: " + sBundle, ex );
                        throw new AssertionFailedException( "Failed to load OSGi bundle: " + sBundle, ex );
                    }
                }
            }

        } catch ( IOException ex ) {
            throw new AssertionFailedException( "Unable to load littleware.properties", ex );
        }

    }

    /**
     * Setup the GUICE injector, use the injector to allocate the registered
     * OSGi bundle activators, inject the SessionManager into
     * the SessionUtil singleton of specified at construction time,
     * and boot into OSGi.
     *
     * @return injector - only intended for access by bootstrap subtypes
     * @exception IllegalStateException if bootstrap has already run
     * @exception IOException on failure to load init daa
     */
    protected Injector bootstrapInternal () {
        if ( ob_bootstrap ) {
            throw new IllegalStateException ( "Bootstrap already run" );
        }
        if ( ovOSGi.isEmpty() ) {
            throw new IllegalStateException( "Empty OSGi bundle list - nothing to do!");
        }

        addInfoFromProperties();

        final Injector injector = Guice.createInjector(
                ovGuice
                );

        if ( obServer ) {
            // Inject the local SessionManager for clients accessing SessionUtil
            final SessionManager mgr_session = injector.getInstance( SessionManager.class );
            injector.injectMembers( SessionUtil.get() );
        }
        
        // Get Guice injected instances of the OSGi BundleActivators,
        // and bootstrap OSGi
        final Map<String,Object> map_felix = new HashMap<String,Object>();
        final List<BundleActivator> v_activate = new ArrayList<BundleActivator> ();
        for( Class<? extends BundleActivator> class_act : ovOSGi ) {
            v_activate.add( injector.getInstance( class_act ) );
        }
        //System.setProperty( "org.osgi.framework.storage", PropertiesLoader.get().getLittleHome().toString() + "/osgi_cache" );
        if ( ! obServer ) {
            // setup cache under little home
            final File cacheDir = PropertiesLoader.get().getLittleHome().
                    getOr( new File( System.getProperty("java.io.tmpdir" ) ) );
            map_felix.put( BundleCache.CACHE_ROOTDIR_PROP, cacheDir.toString() );
        }
        map_felix.put( FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, v_activate );
        ofelix = new Felix( map_felix );
        try {
            ofelix.start();
        } catch (BundleException ex) {
            throw new AssertionFailedException( "Failed to bootstrap Felix OSGi", ex );
        }

        ob_bootstrap = true;
        return injector;
    }

    @Override
    public void shutdown() {
        try {
            olog.log( Level.FINE, "Shutting down ..." );
            ofelix.stop();
            ofelix.waitForStop( 5000 );
        } catch ( RuntimeException ex ) {
            throw ex;
        } catch ( Exception ex ) {
            olog.log( Level.WARNING, "Felix shutdown caught exception", ex );
        }

    }

    @Override
    public void bootstrap () {
        bootstrapInternal();
    }
}
