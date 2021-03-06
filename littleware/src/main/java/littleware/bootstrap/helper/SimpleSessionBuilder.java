package littleware.bootstrap.helper;

import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.bootstrap.AppBootstrap;
import littleware.bootstrap.SessionBootstrap;
import littleware.bootstrap.SessionBootstrap.SessionBuilder;
import littleware.bootstrap.SessionInjector;
import littleware.bootstrap.SessionModule;
import littleware.bootstrap.SessionModuleFactory;

public class SimpleSessionBuilder implements SessionBootstrap.SessionBuilder {

    private static final Logger log = Logger.getLogger(SimpleSessionBuilder.class.getName());
    private final List<SessionModuleFactory> sessionFactoryList = new ArrayList<>();

    {
        log.log(Level.FINE, "Scanning for session factories ...");
        for (SessionModuleFactory moduleFactory : ServiceLoader.load(SessionModuleFactory.class)) {
            log.log(Level.FINE, "Adding session module: {0}", moduleFactory.getClass().getName());
            sessionFactoryList.add(moduleFactory);
        }
    }
    private final AppBootstrap.AppProfile profile;
    private final Injector parentInjector;

    @Inject
    public SimpleSessionBuilder(AppBootstrap.AppProfile profile, Injector parentInjector) {
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
        final SimpleSessionBuilder result = new SimpleSessionBuilder(profile, parentInjector);
        result.sessionFactoryList.clear();
        result.sessionFactoryList.addAll(this.sessionFactoryList);
        return result;
    }

    @Override
    public SessionBootstrap build() {
        final ImmutableList.Builder<SessionModule> sessionBuilder = ImmutableList.builder();
        for (SessionModuleFactory factory : sessionFactoryList) {
            sessionBuilder.add(factory.buildSessionModule(profile));
        }
        return new Bootstrap(sessionBuilder.build(), parentInjector, sessionId);
    }

    private UUID sessionId = UUID.randomUUID();

    @Override
    public UUID getSessionId() {
        return sessionId;
    }

    @Override
    public final void setSessionId(UUID v) {
        sessionId(v);
    }

    @Override
    public SessionBuilder sessionId(UUID v) {
        sessionId = v;
        return this;
    }

    //---------------------------------------------------
    private static class Bootstrap implements SessionBootstrap {

        private final Collection<SessionModule> sessionModuleSet;
        private final Injector parentInjector;
        private final UUID sessionId;

        public Bootstrap(
                ImmutableList<SessionModule> sessionModuleSet,
                Injector injector,
                UUID sessionId) {
            this.parentInjector = injector;
            this.sessionModuleSet = sessionModuleSet;
            this.sessionId = sessionId;
        }

        @Override
        public Collection<SessionModule> getSessionModuleSet() {
            return sessionModuleSet;
        }
        private boolean startOnce = true;

        /**
         * Internal SessionInjector implementation - bound at session boot time
         */
        public static class SimpleSessionInjector implements SessionInjector {

            private Injector injector;

            public SimpleSessionInjector() {
            }

            /**
             * Explicitly set the session injector at bootstrap time
             */
            public void setInjector(Injector value) {
                this.injector = value;
            }

            @Override
            public Injector getInjector() {
                return injector;
            }

            @Override
            public <T> T getInstance(Class<T> clazz) {
                return injector.getInstance(clazz);
            }

            @Override
            public <T> T injectMembers(T injectMe) {
                injector.injectMembers(injectMe);
                return injectMe;
            }
        }

        @Override
        public <T> T startSession(Class<T> clazz) {
            if (!startOnce) {
                throw new IllegalStateException("Session already started");
            }
            startOnce = false;

            final ImmutableList.Builder<SessionModule> modSetBuilder = ImmutableList.builder();
            modSetBuilder.addAll(this.sessionModuleSet);
            {
                final SessionModule module = new SessionModule() {

                    @Override
                    public void configure(Binder binder) {
                        binder.bind(SessionBootstrap.class).toInstance(Bootstrap.this);
                        binder.bind(SessionInjector.class).to(SimpleSessionInjector.class).in(Scopes.SINGLETON);
                    }

                    @Override
                    public Optional<? extends Class<? extends Runnable>> getSessionStarter() {
                        return Optional.empty();
                    }
                };
                modSetBuilder.add(module);
            }

            final Injector childInjector = parentInjector.createChildInjector(modSetBuilder.build());
            ((SimpleSessionInjector) childInjector.getInstance(SessionInjector.class)).setInjector(childInjector);
            for (SessionModule module : modSetBuilder.build()) {
                final Optional<? extends Class<? extends Runnable>> optStarter = module.getSessionStarter();
                if (optStarter.isPresent()) {
                    childInjector.getInstance(optStarter.get()).run();
                }
            }
            if (childInjector.getInstance(Injector.class) != childInjector) {
                throw new IllegalStateException("What the frick ?");
            }
            return childInjector.getInstance(clazz);
        }

        @Override
        public UUID getSessionId() {
            return sessionId;
        }
    }
}
