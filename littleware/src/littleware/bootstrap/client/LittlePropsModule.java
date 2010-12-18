/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.bootstrap.client;

import com.google.inject.Binder;
import java.io.IOException;
import littleware.base.AssertionFailedException;
import littleware.base.PropertiesGuice;
import littleware.bootstrap.client.AppBootstrap.AppProfile;

/**
 * Module sets up PropertiesGuice() bindings from littleware.properties
 */
public class LittlePropsModule extends AbstractClientModule {
    public static class Factory implements AppModuleFactory {

        @Override
        public AppModule build(AppProfile profile) {
            return new LittlePropsModule( profile );
        }

    }

    private LittlePropsModule( AppBootstrap.AppProfile profile ) {
        super( profile );
    }

    @Override
    public void configure( Binder binder ) {
        try {
            (new PropertiesGuice()).configure(binder);
        } catch (IOException ex) {
            throw new AssertionFailedException( "Unexpected failure loading littleware.properties", ex );
        }
    }
}
