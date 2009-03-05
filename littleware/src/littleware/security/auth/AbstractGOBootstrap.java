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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
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
 * the lw.guice_module property.  Similarly loads
 * additional OSGi bundle classes from the lw.osgi_bundle property.
 */
public abstract class AbstractGOBootstrap implements GuiceOSGiBootstrap {
    private static final Logger olog = Logger.getLogger( AbstractGOBootstrap.class.getName() );

    private final List<Module>  ovGuice = new ArrayList<Module>();
    {
        try {
            ovGuice.add(
                    new PropertiesGuice( PropertiesLoader.get().loadProperties() )
                    );
        } catch ( IOException ex ) {
            throw new AssertionFailedException( "Failed default Guice setup", ex );
        }
    }

    private final List<Class<? extends BundleActivator>>  ovOSGi = new ArrayList<Class<? extends BundleActivator>>();
    private final boolean obInjectSessionUtil;
    
    /**
     * Inject bootstrap environment
     * 
     * @param vAddGuice guice modules to add to littleware.properties setup
     * @param vAddOSGi OSGi bundles to boot
     * @param bInjectSessionUtil true to inject SessionUtil with SessionManager - weird corner case,
     *                usually set false.
     */
    protected AbstractGOBootstrap( List<Module> vAddGuice, List<Class<? extends BundleActivator>> vAddOSGi,
            boolean bInjectSessionUtil ) {
        ovGuice.addAll( vAddGuice );
        ovOSGi.addAll( vAddOSGi );
        obInjectSessionUtil = bInjectSessionUtil;
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
    public List<Class<? extends BundleActivator>> getOSGiActivator() {
        return ovOSGi;
    }

    private boolean   ob_bootstrap = false;
    private Felix     ofelix = null;


    /**
     * Setup the GUICE injector, use the injector to allocate the registered
     * OSGi bundle activators, inject the SessionManager into
     * the SessionUtil singleton of specified at construction time,
     * and boot into OSGi.
     *
     * @exception IllegalStateException if bootstrap has already run
     * @exception IOException on failure to load init daa
     */
    public void bootstrap () {
        if ( ob_bootstrap ) {
            throw new IllegalStateException ( "Bootstrap already run" );
        }
        if ( ovOSGi.isEmpty() ) {
            throw new IllegalStateException( "Empty OSGi bundle list - nothing to do!");
        }

        try {
            Properties propLittleware = PropertiesLoader.get().loadProperties();
            String sModules = propLittleware.getProperty( "lw.guice_module", "");
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
            String sBundles = propLittleware.getProperty( "lw.osgi_bundle", "");
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

        Injector injector = Guice.createInjector(
                ovGuice
                );

        if ( obInjectSessionUtil ) {
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
        }
        
        // Get Guice injected instances of the OSGi BundleActivators,
        // and bootstrap OSGi
        Map<String,Object> map_felix = new HashMap<String,Object>();
        List<BundleActivator> v_activate = new ArrayList<BundleActivator> ();
        for( Class<? extends BundleActivator> class_act : ovOSGi ) {
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
            olog.log( Level.FINE, "Shutting down ..." );
            ofelix.stop();
            ofelix.waitForStop( 5000 );
        } catch ( RuntimeException ex ) {
            throw ex;
        } catch ( Exception ex ) {
            olog.log( Level.WARNING, "Felix shutdown caught exception", ex );
        }

    }

}