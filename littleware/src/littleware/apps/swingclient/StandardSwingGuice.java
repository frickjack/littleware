/*
 * Copyright (c) 2007,2008 Reuben Pasquini
 * All Rights Reserved
 */

package littleware.apps.swingclient;

import com.google.inject.Binder;
import com.google.inject.Provider;
import com.google.inject.Module;

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
    
    
    private static final IconLibrary  olib_icon  = new WebIconLibrary ();
    
    
    /**
     * Provide hook by which client may customize the globally
     * injected icon library's properties.
     * 
     * @return the globally shared icon library
     */
    public static IconLibrary getIconLibrary () {
        return olib_icon;
    }
    
    @Override
    public void configure( Binder binder ) {
        binder.bind( AssetViewFactory.class ).to( SimpleAssetViewFactory.class );
        binder.bind( AssetEditorFactory.class ).to( EditorAssetViewFactory.class );
        binder.bind( IconLibrary.class ).toProvider( new Provider<IconLibrary> () {
            public IconLibrary get () {
                return olib_icon;
            }
        });
    }

}
