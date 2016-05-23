/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.bootstrap.helper;

import littleware.base.Option;
import littleware.base.Options;
import littleware.bootstrap.AppBootstrap;
import littleware.bootstrap.AppBootstrap.AppProfile;
import littleware.bootstrap.AppModule;

public abstract class AbstractAppModule implements AppModule {
    private final AppProfile profile;


    public AbstractAppModule( AppBootstrap.AppProfile profile
            ) {
        this.profile = profile;
    }

    @Override
    public AppProfile getProfile() {
        return profile;
    }

    @Override
    public Option<? extends Class<? extends LifecycleCallback>> getCallback() {
        return Options.empty();
    }

}
