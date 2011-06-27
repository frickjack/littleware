/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.gson.internal;

import com.google.inject.Inject;
import com.google.inject.Provider;
import littleware.asset.LittleHome;
import littleware.asset.gson.AbstractAssetAdapter;


public class HomeAdapter extends AbstractAssetAdapter {
    @Inject
    public HomeAdapter( Provider<LittleHome.HomeBuilder> builderFactory ) {
        super( LittleHome.HOME_TYPE, builderFactory );
    }
}
