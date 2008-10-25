/*
 * Copyright (c) 2007,2008 Reuben Pasquini
 * All Rights Reserved
 */

package littleware.apps.swingclient;

import com.google.inject.Binder;
import com.google.inject.Provider;
import com.google.inject.Module;

import com.google.inject.Scopes;
import littleware.apps.client.*;
import littleware.security.auth.ClientServiceGuice;
import littleware.security.auth.SessionHelper;

/**
 * Specialize ClientServiceGuice to include bindings
 * for the littleware.apps.swingclient package.
 * 
 * @author pasquini
 */
public class StandardSwingGuice implements Module {
    
    
    /** 
     * Client must inject SessionHelper dependency 
     * by hand via setSessionHelper
     */
    public StandardSwingGuice () {}
    
    
    @Override
    public void configure( Binder binder ) {
        binder.bind( AssetViewFactory.class ).to( SimpleAssetViewFactory.class );
        binder.bind( AssetEditorFactory.class ).to( EditorAssetViewFactory.class );
        binder.bind( IconLibrary.class ).to( WebIconLibrary.class ).in( Scopes.SINGLETON );
    }

}
