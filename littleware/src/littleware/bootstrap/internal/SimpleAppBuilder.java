/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.bootstrap.internal;

import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.google.inject.Injector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Logger;
import littleware.bootstrap.AppBootstrap;
import littleware.bootstrap.helper.AbstractLittleBootstrap;
import littleware.bootstrap.AppBootstrap.AppBuilder;
import littleware.bootstrap.AppBootstrap.AppProfile;
import littleware.bootstrap.AppModule;
import littleware.bootstrap.AppModuleFactory;
import littleware.bootstrap.LittleBootstrap;
import littleware.bootstrap.SessionBootstrap;
import littleware.bootstrap.helper.AbstractAppModule;
import littleware.bootstrap.helper.SimpleSessionBuilder;

public class SimpleAppBuilder implements AppBootstrap.AppBuilder {

    private static final Logger log = Logger.getLogger(SimpleAppBuilder.class.getName());
    // ---------------
    private final List<AppModuleFactory> factoryList = new ArrayList<AppModuleFactory>();
    private AppProfile profile = AppProfile.SwingApp;

    {
        for (AppModuleFactory moduleFactory : ServiceLoader.load(AppModuleFactory.class)) {
            factoryList.add(moduleFactory);
        }
    }

    // ---------------
    @Override
    public Collection<AppModuleFactory> getModuleSet() {
        return ImmutableList.copyOf(factoryList);
    }

    @Override
    public AppBuilder addModuleFactory(AppModuleFactory factory) {
        factoryList.add(factory);
        return this;
    }

    @Override
    public AppBuilder removeModuleFactory(AppModuleFactory factory) {
        factoryList.remove(factory);
        return this;
    }

    @Override
    public AppBuilder profile(AppProfile value) {
        this.profile = value;
        return this;
    }

    // ---------------
    private static class Bootstrap extends AbstractLittleBootstrap<AppModule> implements AppBootstrap {

        private final AppProfile profile;
        private final Collection<? extends AppModule> moduleSet;

        public Bootstrap(Collection<? extends AppModule> moduleSet, AppBootstrap.AppProfile profile) {
            super(moduleSet);
            this.profile = profile;
            this.moduleSet = moduleSet;
        }

        @Override
        public AppBootstrap.AppProfile getProfile() {
            return profile;
        }

        private Injector appInjector = null;
        
        /**
         * Application-level bootstrap - initializes app-level injector on first call,
         * then just returns that cached value
         * 
         * @return app-level injector (to create sessions with)
         */
        protected Injector bootstrapApp() {
            if ( null != appInjector ) {
                return appInjector;
            }
            final ImmutableList.Builder<AppModule> builder = ImmutableList.builder();
            builder.addAll( this.moduleSet );
            builder.add(new AppSetupModule(profile, this));
            appInjector = super.osgiBootstrap( Injector.class, builder.build());
            return appInjector;
        }

        @Override
        public SessionBootstrap.SessionBuilder newSessionBuilder() {
            return new SimpleSessionBuilder( getProfile(), bootstrapApp() );
        }

        @Override
        public void bootstrap() {
            bootstrap( Injector.class );
        }

        @Override
        public <T> T bootstrap(Class<T> bootClass) {
            return newSessionBuilder().build().startSession(bootClass);
        }

    }

    // ---------------
    private static class AppSetupModule extends AbstractAppModule {

        private final AppBootstrap boot;

        public AppSetupModule(AppBootstrap.AppProfile profile, AppBootstrap boot) {
            super(profile);
            this.boot = boot;
        }

        @Override
        public void configure(Binder binder) {
            binder.bind(LittleBootstrap.class).to(AppBootstrap.class);
            binder.bind(AppBootstrap.class).toInstance(boot);
            binder.bind( AppBootstrap.AppProfile.class ).toInstance( boot.getProfile() );
        }
    }

    // ---------------
    @Override
    public AppBootstrap build() {
        final ImmutableList.Builder<AppModule> builder = ImmutableList.builder();
        for (AppModuleFactory factory : factoryList) {
            builder.add(factory.build(profile));
        }
        return new Bootstrap(builder.build(), profile);
    }
}
