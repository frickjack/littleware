/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.client.bootstrap.internal;

import littleware.bootstrap.AppModule;
import littleware.bootstrap.AppModuleFactory;
import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import littleware.asset.AssetManager;
import littleware.asset.AssetSearchManager;
import littleware.asset.client.bootstrap.ClientBootstrap;
import littleware.base.AssertionFailedException;
import littleware.bootstrap.helper.AbstractLittleBootstrap;
import littleware.bootstrap.LittleBootstrap;
import littleware.asset.client.bootstrap.ClientBootstrap.ClientBuilder;
import littleware.asset.client.bootstrap.SessionModule;
import littleware.asset.client.bootstrap.SessionModuleFactory;
import littleware.bootstrap.AppBootstrap;
import littleware.bootstrap.AppBootstrap.AppProfile;
import littleware.bootstrap.helper.AbstractAppModule;
import littleware.security.LittleUser;
import littleware.security.auth.LittleSession;
import littleware.security.auth.client.SessionManager;

public class SimpleClientBuilder implements ClientBootstrap.ClientBuilder {

    private static final Logger log = Logger.getLogger(SimpleClientBuilder.class.getName());
    private final List<AppModuleFactory> factoryList = new ArrayList<AppModuleFactory>();
    private final List<SessionModuleFactory> sessionFactoryList = new ArrayList<SessionModuleFactory>();
    private AppProfile profile = AppProfile.SwingApp;

    @Override
    public AppProfile getProfile() {
        return profile;
    }
    
    {
        for (SessionModuleFactory moduleFactory : ServiceLoader.load(SessionModuleFactory.class)) {
            sessionFactoryList.add(moduleFactory);
        }
        for ( final AppModuleFactory moduleFactory : ServiceLoader.load(AppModuleFactory.class)) {
            factoryList.add(moduleFactory);
        }

        if ( factoryList.isEmpty() ) {
            throw new AssertionFailedException( "Failed to find base client modules: " + SessionModuleFactory.class  );
        }
    }

    @Override
    public Collection<AppModuleFactory> getModuleSet() {
        return ImmutableList.copyOf(factoryList);
    }

    @Override
    public ClientBuilder addModuleFactory(AppModuleFactory factory) {
        factoryList.add(factory);
        return this;
    }

    @Override
    public ClientBuilder removeModuleFactory(AppModuleFactory factory) {
        factoryList.remove(factory);
        return this;
    }

    @Override
    public ClientBuilder profile(AppProfile value) {
        this.profile = value;
        return this;
    }


    @Override
    public Collection<SessionModuleFactory> getSessionModuleSet() {
        return ImmutableList.copyOf(sessionFactoryList);
    }

    @Override
    public ClientBuilder addModuleFactory(SessionModuleFactory factory) {
        sessionFactoryList.add( factory );
        return this;
    }

    @Override
    public ClientBuilder removeModuleFactory(SessionModuleFactory factory) {
        sessionFactoryList.remove( factory );
        return this;
    }


    private SimpleClientBuilder copy() {
        final SimpleClientBuilder result = new SimpleClientBuilder();
        result.factoryList.clear();
        result.factoryList.addAll( this.factoryList );
        result.sessionFactoryList.clear();
        result.sessionFactoryList.addAll( this.sessionFactoryList );
        result.profile = this.profile;
        return result;
    }

    @Override
    public ClientBootstrap build() {
        final ImmutableList.Builder<AppModule> appBuilder = ImmutableList.builder();
        final ImmutableList.Builder<SessionModule> sessionBuilder = ImmutableList.builder();
        for (AppModuleFactory factory : factoryList) {
            appBuilder.add(factory.build(profile));
        }
        for( SessionModuleFactory factory : sessionFactoryList ) {
            sessionBuilder.add( factory.build( profile ) );
        }
        return new Bootstrap(appBuilder.build(), sessionBuilder.build(), profile);
    }


    //---------------------------------------------------

    private static class Bootstrap extends AbstractLittleBootstrap<AppModule> implements ClientBootstrap {

        private final AppProfile profile;
        private final Collection<SessionModule>  sessionModuleSet;
        private Injector injector = null;

        public Bootstrap(ImmutableList<? extends AppModule> moduleSet,
                ImmutableList<SessionModule> sessionModuleSet,
                AppProfile profile) {
            super(moduleSet);
            this.profile = profile;
            this.sessionModuleSet = sessionModuleSet;
        }

        @Override
        public AppProfile getProfile() { return profile; }

        @Override
        protected <T> T bootstrap(Class<T> injectTarget, Collection<? extends AppModule> originalModuleSet) {
            final ImmutableList.Builder<AppModule> builder = ImmutableList.builder();
            builder.addAll(originalModuleSet);
            builder.add(new ClientSetupModule( profile, this));
            injector = super.bootstrap( Injector.class, builder.build() );
            return injector.getInstance(injectTarget);
        }

        @Override
        public Collection<SessionModule> getSessionModuleSet() {
            return sessionModuleSet;
        }

        @Override
        public <T> T startSession(Class<T> clazz, String sessionId) {
            final ImmutableList.Builder<SessionModule> builder = ImmutableList.builder();
            builder.addAll( this.sessionModuleSet );
            builder.add( new SessionSetupModule( sessionId ) );
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <T> T startTestSession(Class<T> clazz, String sessionId) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    //----------------------------------------------------------

    public static class LittleSessionProvider implements Provider<LittleSession> {
        @Inject
        public LittleSessionProvider( SessionManager sessionMgr ) {

        }

        @Override
        public LittleSession get() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    //---------------------------------------------

    public static class LittleUserProvider implements Provider<LittleUser> {
        @Inject
        public LittleUserProvider( Subject subject ) {

        }

        @Override
        public LittleUser get() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    //---------------------------------------------

    public static class SubjectProvider implements Provider<Subject> {
        @Inject
        public SubjectProvider( SessionManager sessionMgr ) {

        }

        @Override
        public Subject get() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }


    //----------------------------------------------

    /**
     * Internal module injects SessionHelper
     */
    public static class ClientSetupModule extends AbstractAppModule {
        private final ClientBootstrap bootstrap;


        public ClientSetupModule(
                AppBootstrap.AppProfile profile,
                ClientBootstrap bootstrap) {
            super(profile);
            this.bootstrap = bootstrap;
        }

        @Override
        public void configure(Binder binder) {
            binder.bind( LittleBootstrap.class ).to( AppBootstrap.class );
            binder.bind(AppBootstrap.class).to(ClientBootstrap.class);
            binder.bind(ClientBootstrap.class).toInstance(bootstrap);
            binder.bind( AppBootstrap.AppProfile.class ).toInstance( bootstrap.getProfile() );
        }
    }

    //-------------------------------------------------------------

    public static class SessionSetupModule implements SessionModule {
        public SessionSetupModule( String sessionId ) {

        }

        @Override
        public void configure(Binder binder) {
            binder.bind( AssetSearchManager.class );
            binder.bind( AssetManager.class );
            binder.bind( SessionManager.class );
            binder.bind(LittleSession.class);
            binder.bind(LittleUser.class);
        }

    }


}
