/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.bootstrap;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.base.AssertionFailedException;
import littleware.base.EventBarrier;
import littleware.base.Maybe;
import littleware.base.PropertiesLoader;
import org.apache.felix.framework.Felix;
import org.apache.felix.framework.cache.BundleCache;
import org.apache.felix.framework.util.FelixConstants;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;


public abstract class AbstractLittleBootstrap<T extends LittleModule> implements LittleBootstrap {
    private static final Logger log = Logger.getLogger( AbstractLittleBootstrap.class.getName() );

    private final Collection<? extends T> moduleSet;

    protected AbstractLittleBootstrap( Collection<? extends T> moduleSet ) {
        this.moduleSet = moduleSet;
    }

    @Override
    public final void bootstrap() {
        bootstrap( Injector.class );
    }

    @Override
    public Collection<? extends T> getModuleSet() {
        return moduleSet;
    }
    
    /**
     * Internal activator just triggers a barrier after
     * receiving the OSGi framework-started event.
     */
    private static class SetupActivator implements BundleActivator {

        private final EventBarrier<Object> barrier;

        @Inject
        public SetupActivator(EventBarrier<Object> barrier) {
            this.barrier = barrier;
        }

        @Override
        public void start(final BundleContext ctx) throws Exception {
            ctx.addFrameworkListener(new FrameworkListener() {

                @Override
                public synchronized void frameworkEvent(FrameworkEvent evt) {
                    if ((evt.getType() == FrameworkEvent.STARTED)) {
                        ctx.removeFrameworkListener(this);
                        log.log(Level.FINE, "Triggering startup barrier ...");
                        barrier.publishEventData(null);
                    }
                }
            });
        }

        @Override
        public void stop(BundleContext ctx) throws Exception {
        }
    }

    private boolean bootstrapDone = false;
    private Felix   felix = null;


    protected <R> R bootstrap( Class<R> bootClass, Collection<? extends T> moduleSet ) {
        if ( bootstrapDone ) {
            throw new IllegalStateException( "Bootstrap can only run once" );
        }
        final Injector injector = Guice.createInjector(
                moduleSet
                );

        // Get Guice injected instances of the OSGi BundleActivators,
        // and bootstrap OSGi
        final Map<String, Object> felixPropertyMap = new HashMap<String, Object>();
        final List<BundleActivator> activatorList = new ArrayList<BundleActivator>();
        for( LittleModule module : moduleSet ) {
            final Maybe<Class<? extends BundleActivator>> maybe = module.getActivator();
            if ( maybe.isSet() ) {
                activatorList.add(
                        injector.getInstance( maybe.get() )
                        );
            }
        }

        final EventBarrier<Object> barrier = new EventBarrier<Object>();
        activatorList.add(
                new SetupActivator( barrier )
                );
        //System.setProperty( "org.osgi.framework.storage", PropertiesLoader.get().getLittleHome().toString() + "/osgi_cache" );
        {
            // setup cache under little home
            final File cacheDir = PropertiesLoader.get().getLittleHome().
                    getOr(new File(System.getProperty("java.io.tmpdir")));
            felixPropertyMap.put(BundleCache.CACHE_ROOTDIR_PROP, cacheDir.toString());
        }
        felixPropertyMap.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, activatorList);
        felix = new Felix(felixPropertyMap);
        try {
            felix.start();
        } catch ( Exception ex ) {
            log.log( Level.SEVERE, "OSGi bootstrap failed", ex );
            throw new AssertionFailedException("Failed to bootstrap Felix OSGi", ex);
        }
        log.log( Level.FINE, "Waiting for OSGi startup ..." );
        try {
            barrier.waitForEventData();
        } catch (InterruptedException ex) {
            throw new IllegalStateException( "Bootstrap interrupted", ex );
        }
        bootstrapDone = true;
        return injector.getInstance(bootClass);
    }

    @Override
    public final <R> R bootstrap(Class<R> bootClass) {
        return bootstrap( bootClass, moduleSet );
    }

    @Override
    public void shutdown() {
        if ( null == felix ) {
            throw new IllegalStateException( "Cannot shutdown if bootstrap failed" );
        }
        try {
            log.log(Level.FINE, "Shutting down ...");
            felix.stop();
            felix.waitForStop(5000);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            log.log(Level.WARNING, "Felix shutdown caught exception", ex);
        } finally {
            felix = null;
        }
    }

}
