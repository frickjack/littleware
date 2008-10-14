/*
 * Copyright 2008 Reuben Pasquini
 * All Rights Reserved
 */

package littleware.apps.client;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;


/**
 * Typical Guice setup for littleware.apps.client setup
 */
public class StandardClientGuice implements Module {
    
    @Override
    public void configure( Binder binder ) {
        binder.bind( AssetModelLibrary.class ).to( SimpleAssetModelLibrary.class ).in( Scopes.SINGLETON );
    }
}
