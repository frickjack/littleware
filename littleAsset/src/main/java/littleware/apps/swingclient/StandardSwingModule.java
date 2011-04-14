/*
 * Copyright (c) 2007,2008 Reuben Pasquini
 * All Rights Reserved
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.swingclient;

import com.google.inject.Binder;

import com.google.inject.Scopes;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import littleware.apps.client.*;
import littleware.asset.client.bootstrap.AbstractClientModule;
import littleware.asset.client.bootstrap.ClientModule;
import littleware.asset.client.bootstrap.ClientModuleFactory;
import littleware.bootstrap.AppBootstrap;
import littleware.bootstrap.AppBootstrap.AppProfile;

/**
 * Specialize ClientServiceGuice to include bindings
 * for the littleware.apps.swingclient package.
 * 
 * @author pasquini
 */
public class StandardSwingModule extends AbstractClientModule {
    public static class Factory implements ClientModuleFactory {

        @Override
        public ClientModule build(AppProfile profile) {
            return new StandardSwingModule( profile );
        }

    }
    
    /** 
     * Client must inject SessionHelper dependency 
     * by hand via setSessionHelper
     */
    private StandardSwingModule ( AppBootstrap.AppProfile profile ) {
        super( profile );
    }
    
    
    @Override
    public void configure( Binder binder ) {
        binder.bind( AssetViewFactory.class ).to( SimpleAssetViewFactory.class ).in( Scopes.SINGLETON );
        binder.bind( AssetEditorFactory.class ).to( EditorAssetViewFactory.class ).in( Scopes.SINGLETON );
        binder.bind( IconLibrary.class ).to( WebIconLibrary.class ).in( Scopes.SINGLETON );
        binder.bind( ListModel.class ).to( DefaultListModel.class );
    }

}
