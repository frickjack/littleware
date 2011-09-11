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
import littleware.bootstrap.AppBootstrap.AppProfile;
import littleware.bootstrap.SessionModule;
import littleware.bootstrap.SessionModuleFactory;
import littleware.lgo.LgoServiceModule;

/**
 * Guice module for bootstrapping the LittleGo 
 * application.  Sets up easy Lgo implementation.
 */
public class LgoModuleFactory implements SessionModuleFactory {

    @Override
    public SessionModule buildSessionModule(AppProfile profile) {
        return new LgoModule();
    }

    public static class LgoModule implements LgoServiceModule {

        public Class<? extends Runnable> getSessionStarter() {
            return SessionModule.NullStarter.class;
        }

        public LgoModule(){}

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
    }
}