/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.lgo;


import com.google.common.collect.ImmutableList;
import java.util.Collection;
import littleware.lgo.LgoCommand;

import com.google.inject.Binder;
import com.google.inject.Inject;
import littleware.asset.gson.internal.GsonProvider;
import littleware.bootstrap.AppBootstrap;
import littleware.bootstrap.AppBootstrap.AppProfile;
import littleware.bootstrap.SessionModule;
import littleware.bootstrap.SessionModuleFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Guice module for bootstrapping the LittleGo 
 * application.  Sets up easy Lgo implementation.
 */
public class LgoModule implements SessionModule {

    public Class<? extends Runnable> getSessionStarter() {
        return Activator.class;
    }

    public static class Factory implements SessionModuleFactory {

        @Override
        public SessionModule build(AppProfile profile) {
            return new LgoModule(profile);
        }
    }

    private LgoModule(AppBootstrap.AppProfile profile) {
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
    }

    public Collection<Class<? extends LgoCommand.LgoBuilder>> getLgoCommands() {
        return lgoCommands;
    }

    private final Collection<Class<? extends LgoCommand.LgoBuilder>> lgoCommands;
    {
        final ImmutableList.Builder<Class<? extends LgoCommand.LgoBuilder>> builder =
                ImmutableList.builder();
        builder.add(DeleteAssetCommand.Builder.class);
        builder.add(ListChildrenCommand.Builder.class);
        builder.add(GetAssetCommand.Builder.class);
        builder.add(CreateFolderCommand.Builder.class);
        builder.add(CreateUserCommand.Builder.class);
        builder.add(GetByNameCommand.Builder.class);
        builder.add(GetRootPathCommand.Builder.class);
        lgoCommands = builder.build();
    }

    /**
     * Lgo BundleActivator registers lgo commands with
     * the LgoCommandDictionary
     */
    public static class Activator implements Runnable {

        /** Inject dependencies */
        @Inject
        public Activator(
                GsonProvider gsonProvider) {
        }

        /** NOOP */
        @Override
        public void run() {
        }

    }
}
