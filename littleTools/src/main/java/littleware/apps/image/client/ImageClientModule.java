/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.image.client;

import com.google.inject.Binder;
import com.google.inject.Scopes;
import littleware.apps.image.ImageManager;
import littleware.apps.image.ThumbManager;
import littleware.asset.client.bootstrap.helper.AbstractClientModule;
import littleware.asset.client.bootstrap.SessionModule;
import littleware.asset.client.bootstrap.SessionModuleFactory;
import littleware.bootstrap.AppBootstrap;
import littleware.bootstrap.AppBootstrap.AppProfile;

/**
 * Setup littleware.apps.misc package bindings
 */
public class ImageClientModule extends AbstractClientModule {
    public static class Factory implements SessionModuleFactory {

        @Override
        public SessionModule build(AppProfile profile ) {
            return new ImageClientModule( profile );
        }

    }

    private ImageClientModule( AppBootstrap.AppProfile profile ) {
        super( profile );
    }

    @Override
    public void configure(Binder binder) {
        binder.bind( ImageManager.class ).to( SimpleImageManager.class ).in(Scopes.SINGLETON);
        binder.bind( ThumbManager.class ).to( SimpleThumbManager.class ).in(Scopes.SINGLETON);
    }

}
