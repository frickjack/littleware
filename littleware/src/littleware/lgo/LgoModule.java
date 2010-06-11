/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.lgo;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import java.util.Arrays;
import littleware.bootstrap.client.AbstractClientModule;
import littleware.bootstrap.client.AppBootstrap;
import littleware.bootstrap.client.AppBootstrap.AppProfile;
import littleware.bootstrap.client.AppModule;
import littleware.bootstrap.client.AppModuleFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Guice module for bootstrapping the LittleGo
 * application.  Sets up easy Lgo implementation.
 */
public class LgoModule extends AbstractClientModule {

    public static class Factory implements AppModuleFactory {

        @Override
        public AppModule build(AppProfile profile) {
            return new LgoModule(profile);
        }
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
                Provider<EzHelpCommand> comHelp,
                Provider<XmlEncodeCommand> comXml
            ) {
            for (Provider<? extends LgoCommand<?, ?>> command : // need to move this into a properties file
                    Arrays.asList(
                    comHelp, comXml )) {
                commandMgr.setCommand(helpMgr, command);
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
