/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.tracker.client;

import com.google.inject.Binder;
import com.google.inject.Scopes;
import littleware.apps.tracker.MemberIndex;
import littleware.apps.tracker.internal.SimpleIndexBuilder;
import littleware.apps.tracker.internal.SimpleZipUtil;
import littleware.apps.tracker.ZipUtil;
import littleware.bootstrap.AppBootstrap.AppProfile;
import littleware.bootstrap.AppModule;
import littleware.bootstrap.AppModuleFactory;
import littleware.bootstrap.helper.NullActivator;
import org.osgi.framework.BundleActivator;

/**
 * Application-mode module just binds implementations
 * for ZipUtil and other utility classes.
 */
public class TrackerAppModule implements AppModule {
    private final AppProfile profile;

    public static class AppFactory implements AppModuleFactory {

        @Override
        public AppModule build(AppProfile profile) {
            return new TrackerAppModule( profile );
        }
    }

    public TrackerAppModule(AppProfile profile) {
        this.profile = profile;
    }

    @Override
    public AppProfile getProfile() {
        return profile;
    }

    @Override
    public Class<? extends BundleActivator> getActivator() {
        return NullActivator.class;
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(ZipUtil.class).to(SimpleZipUtil.class).in(Scopes.SINGLETON);
        binder.bind(MemberIndex.IndexBuilder.class).to(SimpleIndexBuilder.class);
    }
}

