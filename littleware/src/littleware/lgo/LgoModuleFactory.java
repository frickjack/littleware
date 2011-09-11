/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.lgo;

import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Scopes;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.bootstrap.AppBootstrap;
import littleware.bootstrap.AppBootstrap.AppProfile;
import littleware.bootstrap.AppModuleFactory;
import littleware.bootstrap.SessionBootstrap;
import littleware.bootstrap.SessionInjector;
import littleware.bootstrap.SessionModule;
import littleware.bootstrap.SessionModuleFactory;
import littleware.bootstrap.helper.AbstractAppModule;
import littleware.bootstrap.helper.NullActivator;

/**
 * Combined AppModuleFactory and SessionModuleFactory for configuring
 * lgo commands with the littleware runtime.  The LGO commands go into session scope,
 * but some shared resources (like the help loader) are in application scope.
 */
public class LgoModuleFactory implements AppModuleFactory, SessionModuleFactory {

    @Override
    public AppModule build(AppProfile profile) {
        return new AppModule(profile);
    }

    @Override
    public SessionModule buildSessionModule(AppProfile profile) {
        return new LgoSessionModule();
    }

    /**
     * Application scope bootstrap module
     */
    public static class AppModule extends AbstractAppModule {

        private static final Logger log = Logger.getLogger(LgoModuleFactory.class.getName());

        private AppModule(AppBootstrap.AppProfile profile) {
            super(profile);
        }

        @Override
        public void configure(Binder binder) {
            // Use provider - problem with class loader in Tomcat environment
            binder.bind(LgoHelpLoader.class).to(XmlLgoHelpLoader.class).in(Scopes.SINGLETON);
        }

        @Override
        public Class<NullActivator> getActivator() {
            return NullActivator.class;
        }
    }

    public static class LgoSessionModule implements LgoServiceModule {

        private final Collection<Class<? extends LgoCommand.LgoBuilder>> lgoCommands;

        {
            final ImmutableList.Builder<Class<? extends LgoCommand.LgoBuilder>> builder = ImmutableList.builder();
            lgoCommands = builder.add(EzHelpCommand.class).add(XmlEncodeCommand.class).build();
        }

        @Override
        public Collection<Class<? extends LgoCommand.LgoBuilder>> getLgoCommands() {
            return lgoCommands;
        }

        @Override
        public Class<? extends Runnable> getSessionStarter() {
            return SessionStarter.class;
        }

        @Override
        public void configure(Binder binder) {
            binder.bind(LgoCommandDictionary.class).to(EzLgoCommandDictionary.class).in(Scopes.SINGLETON);
        }
    }

    /**
     * Lgo SessionStarter scans session modules registered with SessionBootstrap for
     * LgoServiceModule implementations, and registers lgo commands with
     * the LgoCommandDictionary
     */
    public static class SessionStarter implements Runnable {

        private static final Logger log = Logger.getLogger(SessionStarter.class.getName());

        /** Inject dependencies */
        @Inject
        public SessionStarter(
                LgoCommandDictionary commandMgr,
                LgoHelpLoader helpMgr,
                SessionInjector injector,
                SessionBootstrap bootstrap) {
            for (SessionModule module : bootstrap.getSessionModuleSet()) {
                if (module instanceof LgoServiceModule) {
                    for (Class<? extends LgoCommand.LgoBuilder> commandClass : ((LgoServiceModule) module).getLgoCommands()) {
                        log.log(Level.FINE, "Register lgo command: {0}", commandClass.getName());
                        commandMgr.setCommand(helpMgr, injector.getInjector().getProvider(commandClass));
                    }
                }
            }
        }

        /** NOOP */
        @Override
        public void run() {
        }
    }
}