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
import com.google.inject.Injector;
import com.google.inject.Scopes;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.bootstrap.AppBootstrap;
import littleware.bootstrap.AppBootstrap.AppProfile;
import littleware.bootstrap.AppModule;
import littleware.bootstrap.AppModuleFactory;
import littleware.bootstrap.LittleBootstrap;
import littleware.bootstrap.LittleModule;
import littleware.bootstrap.helper.AbstractAppModule;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Guice module for bootstrapping the LittleGo
 * application.  Sets up easy Lgo implementation.
 */
public class LgoModule extends AbstractAppModule implements LgoServiceModule {
    private static final Logger log = Logger.getLogger( LgoModule.class.getName() );
    
    public static class Factory implements AppModuleFactory {

        @Override
        public AppModule build(AppProfile profile) {
            return new LgoModule(profile);
        }
    }

    private final Collection<Class<? extends LgoCommand.LgoBuilder>> lgoCommands;
    {
        final ImmutableList.Builder<Class<? extends LgoCommand.LgoBuilder>> builder = ImmutableList.builder();
        lgoCommands = builder.add( EzHelpCommand.class ).add( XmlEncodeCommand.class ).build();
    }

    @Override
    public Collection<Class<? extends LgoCommand.LgoBuilder>> getLgoCommands() {
        return lgoCommands;
    }


    private LgoModule(AppBootstrap.AppProfile profile) {
        super(profile);
    }

    /**
     * If we decide to extend littlego into a shell
     * or BSF/scripting environment, then <br />
     * TODO:
     *      <ul>
     *      <li> Move command mapping to a properties file</li>
     *      <li> Setup XML multilingual help system </li>
     *      </ul>
     * @param binder
     */
    @Override
    public void configure(Binder binder) {
        // Use provider - problem with class loader in Tomcat environment
        binder.bind(LgoCommandDictionary.class).to(EzLgoCommandDictionary.class).in(Scopes.SINGLETON);
        binder.bind(LgoHelpLoader.class).to(XmlLgoHelpLoader.class).in(Scopes.SINGLETON);
    }

    @Override
    public Class<Activator> getActivator() {
        return Activator.class;
    }

    /**
     * Lgo BundleActivator registers lgo commands with
     * the LgoCommandDictionary
     */
    public static class Activator implements BundleActivator {

        /** Inject dependencies */
        @Inject
        public Activator(
                LgoCommandDictionary commandMgr,
                LgoHelpLoader helpMgr,
                Injector injector,
                LittleBootstrap bootstrap
            ) {
            for( LittleModule module : bootstrap.getModuleSet() ) {
                if ( module instanceof LgoServiceModule ) {
                    for( Class<? extends LgoCommand.LgoBuilder> commandClass : ((LgoServiceModule) module).getLgoCommands() ) {
                        log.log( Level.FINE, "Register lgo command: {0}", commandClass.getName());
                        commandMgr.setCommand(helpMgr, injector.getProvider(commandClass));
                    }
                }
            }
        }

        /** NOOP */
        @Override
        public void start(BundleContext bc) throws Exception {
        }

        /** NOOP */
        @Override
        public void stop(BundleContext bc) throws Exception {
        }
    }
}
