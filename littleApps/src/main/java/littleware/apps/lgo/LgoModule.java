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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Binder;
import com.google.inject.Scopes;
import littleware.bootstrap.client.AbstractClientModule;
import littleware.bootstrap.client.AppBootstrap;
import littleware.bootstrap.client.AppBootstrap.AppProfile;
import littleware.bootstrap.client.ClientModule;
import littleware.bootstrap.client.ClientModuleFactory;

/**
 * Guice module for bootstrapping the LittleGo 
 * application.  Sets up easy Lgo implementation.
 */
public class LgoModule extends AbstractClientModule {

    public static class Factory implements ClientModuleFactory {

        @Override
        public ClientModule build(AppProfile profile ) {
            return new LgoModule( profile  );
        }

    }


    private LgoModule( AppBootstrap.AppProfile profile ) {
        super( profile );
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
    public void configure( Binder binder ) {
        // Use provider - problem with class loader in Tomcat environment
        binder.bind( LgoCommandDictionary.class ).to( EzLgoCommandDictionary.class )
                .in( Scopes.SINGLETON );
        binder.bind( LgoHelpLoader.class ).to( XmlLgoHelpLoader.class )
                .in( Scopes.SINGLETON );
        binder.bind( GsonBuilder.class ).toInstance( new GsonBuilder() );
        binder.bind( Gson.class ).toProvider( GsonProvider.class );
    }
}