/*
 * Copyright (c) 2007,2008 Controlled Monitoring Marlborough NZ
 * All Rights Reserved
 */

package littleware.apps.swingclient;

import com.google.inject.Binder;
import com.google.inject.Provider;

import littleware.security.auth.ClientServiceGuice;
import littleware.security.auth.SessionHelper;


/**
 * Specialize ClientServiceGuice to include bindings
 * for the littleware.apps.swingclient package.
 * 
 * @author pasquini
 */
public class TypicalClientGuice extends ClientServiceGuice {
    
    /**
     * Constructor sets the helper this module is associated with
     * 
     * @param helper
     */
    public TypicalClientGuice( SessionHelper helper ) {
        super( helper );
    }
    
    private static final IconLibrary  olib_icon  = new WebIconLibrary ();
    
    private final AssetModelLibrary   olib_asset = new SimpleAssetModelLibrary ();
    
    /**
     * Provide hook by which client may customize the globally
     * injected icon library's properties.
     * 
     * @return the globally shared icon library
     */
    public static IconLibrary getIconLibrary () {
        return olib_icon;
    }
    
    /**
     * Call through to super class, then
     * register swingclient package bindings.
     * 
     * @param binder
     */
    @Override
    public void configure( Binder binder ) {
        super.configure( binder );
        binder.bind( AssetModelLibrary.class ).toProvider( new Provider<AssetModelLibrary> () {
            public AssetModelLibrary get () {
                return olib_asset;
            }
        }
                );
        binder.bind( IconLibrary.class ).toProvider( new Provider<IconLibrary> () {
            public IconLibrary get () {
                return olib_icon;
            }
        });
    }

}
