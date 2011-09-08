/*
 * Copyright 2011 http://code.google.com/p/littleware
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.bootstrap.helper;

import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.base.AssertionFailedException;
import littleware.bootstrap.AppBootstrap;
import littleware.bootstrap.SessionBootstrap;
import littleware.bootstrap.SessionBootstrap.SessionBuilder;
import littleware.bootstrap.SessionModule;
import littleware.bootstrap.SessionModuleFactory;



public class SimpleSessionBuilder implements SessionBootstrap.SessionBuilder {

    private static final Logger log = Logger.getLogger(SimpleSessionBuilder.class.getName());
    private final List<SessionModuleFactory> sessionFactoryList = new ArrayList<SessionModuleFactory>();

    {
        log.log( Level.FINE, "Scanning for session factories ..." );
        for (SessionModuleFactory moduleFactory : ServiceLoader.load(SessionModuleFactory.class)) {
            log.log( Level.FINE, "Adding session module: {0}", moduleFactory.getClass().getName());
            sessionFactoryList.add(moduleFactory);
        }
    }
    private final AppBootstrap.AppProfile profile;
    private final Injector parentInjector;

    @Inject
    public SimpleSessionBuilder( AppBootstrap.AppProfile profile, Injector parentInjector ) {
        this.profile = profile;
        this.parentInjector = parentInjector;
    }


    @Override
    public Collection<SessionModuleFactory> getSessionModuleSet() {
        return ImmutableList.copyOf(sessionFactoryList);
    }

    @Override
    public SessionBuilder addModuleFactory(SessionModuleFactory factory) {
        sessionFactoryList.add(factory);
        return this;
    }

    @Override
    public SessionBuilder removeModuleFactory(SessionModuleFactory factory) {
        sessionFactoryList.remove(factory);
        return this;
    }

    private SimpleSessionBuilder copy() {
        final SimpleSessionBuilder result = new SimpleSessionBuilder( profile, parentInjector );
        result.sessionFactoryList.clear();
        result.sessionFactoryList.addAll(this.sessionFactoryList);
        return result;
    }

    @Override
    public SessionBootstrap build() {
        final ImmutableList.Builder<SessionModule> sessionBuilder = ImmutableList.builder();
        for (SessionModuleFactory factory : sessionFactoryList) {
            sessionBuilder.add(factory.build(profile));
        }
        return new Bootstrap( sessionBuilder.build(), parentInjector);
    }

    //---------------------------------------------------
    private static class Bootstrap implements SessionBootstrap {
        private final Collection<SessionModule> sessionModuleSet;
        private final Injector parentInjector;
        private final String   sessionId = UUID.randomUUID().toString();

        public Bootstrap(
                ImmutableList<SessionModule> sessionModuleSet,
                Injector injector) {
            this.parentInjector = injector;
            this.sessionModuleSet = sessionModuleSet;
        }



        @Override
        public Collection<SessionModule> getSessionModuleSet() {
            return sessionModuleSet;
        }


        private boolean startOnce = true;
        
        @Override
        public <T> T startSession(Class<T> clazz) {
            if ( ! startOnce ) {
                throw new IllegalStateException( "Session already started" );
            }
            startOnce = false;
            
            final ImmutableList.Builder<SessionModule> modSetBuilder = ImmutableList.builder();
            modSetBuilder.addAll( this.sessionModuleSet );
            {
                final SessionModule module = new SessionModule(){
                    @Override
                    public void configure( Binder binder ) {
                        binder.bind( SessionBootstrap.class ).toInstance( Bootstrap.this );
                    }

                    @Override
                    public Class<? extends Runnable> getSessionStarter() {
                        return SessionModule.NullStarter.class;
                    }

                };
                modSetBuilder.add( module );
            }

            
            final Injector childInjector = parentInjector.createChildInjector( modSetBuilder.build() );
            for( SessionModule module : modSetBuilder.build() ) {
                childInjector.getInstance( module.getSessionStarter() ).run();
            }
            if ( childInjector.getInstance( Injector.class ) != childInjector ) {
                throw new AssertionFailedException( "What the frick ?" );
            }
            return childInjector.getInstance(clazz);
        }

        @Override
        public String getSessionId() {
            return sessionId;
        }

    }

}
