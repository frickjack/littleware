/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.lgo;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import littleware.lgo.LgoCommand;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Binder;
import com.google.inject.Inject;
import littleware.asset.client.bootstrap.AbstractClientModule;
import littleware.asset.client.bootstrap.ClientModule;
import littleware.asset.client.bootstrap.ClientModuleFactory;
import littleware.bootstrap.AppBootstrap;
import littleware.bootstrap.AppBootstrap.AppProfile;
import littleware.lgo.LgoServiceModule;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Guice module for bootstrapping the LittleGo 
 * application.  Sets up easy Lgo implementation.
 */
public class LgoModule extends AbstractClientModule implements LgoServiceModule {

    public static class Factory implements ClientModuleFactory {

        @Override
        public ClientModule build(AppProfile profile) {
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
        binder.bind(GsonBuilder.class).toInstance(new GsonBuilder());
        binder.bind(Gson.class).toProvider(GsonProvider.class);
    }

    @Override
    public Collection<Class<? extends LgoCommand.LgoBuilder>> getLgoCommands() {
        return lgoCommands;
    }

    @Override
    public Class<Activator> getActivator() {
        return Activator.class;
    }
    private final Collection<Class<? extends LgoCommand.LgoBuilder>> lgoCommands;
    {
        final ImmutableList.Builder<Class<? extends LgoCommand.LgoBuilder>> builder =
                ImmutableList.builder();
        builder.add(LgoBrowserCommand.Builder.class);
        builder.add(DeleteAssetCommand.Builder.class);
        builder.add(ListChildrenCommand.Builder.class);
        builder.add(GetAssetCommand.Builder.class);
        builder.add(CreateFolderCommand.Builder.class);
        builder.add(CreateUserCommand.Builder.class);
        builder.add(GetByNameCommand.Builder.class);
        builder.add(SetImageCommand.Builder.class);
        builder.add(GetRootPathCommand.Builder.class);
        lgoCommands = builder.build();
    }

    /**
     * Lgo BundleActivator registers lgo commands with
     * the LgoCommandDictionary
     */
    public static class Activator implements BundleActivator {

        /** Inject dependencies */
        @Inject
        public Activator(
                GsonProvider gsonProvider) {
            gsonProvider.registerSerializer(SimpleAssetListBuilder.AssetList.class,
                    new SimpleAssetListBuilder.GsonSerializer());
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
