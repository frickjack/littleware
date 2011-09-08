/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.bootstrap.helper;

import com.google.inject.Binder;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.base.AssertionFailedException;
import littleware.base.PropertiesGuice;
import littleware.base.cache.Cache;
import littleware.base.cache.InMemoryCacheBuilder;
import littleware.bootstrap.AppBootstrap;
import littleware.bootstrap.AppBootstrap.AppProfile;
import littleware.bootstrap.AppModule;
import littleware.bootstrap.AppModuleFactory;
import littleware.bootstrap.SessionBootstrap;

/**
 * Module sets up PropertiesGuice() bindings from littleware.properties
 */
public class LittlePropsModule extends AbstractAppModule {
    private static final Logger log = Logger.getLogger( LittlePropsModule.class.getName() );

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
            log.log( Level.FINE, "Configuring LittlePropsModule ..." );
            PropertiesGuice.build().configure(binder);
            binder.bind( Cache.Builder.class ).to( InMemoryCacheBuilder.class );
            //binder.bind( SessionBootstrap.SessionBuilder.class ).to( SimpleSessionBuilder.class );
        } catch (IOException ex) {
            throw new AssertionFailedException( "Unexpected failure loading littleware.properties", ex );
        }
    }
}
