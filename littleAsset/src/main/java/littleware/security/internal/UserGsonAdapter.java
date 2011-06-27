/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security.internal;

import com.google.inject.Inject;
import com.google.inject.Provider;
import littleware.asset.gson.AbstractAssetAdapter;
import littleware.security.LittleUser;


public class UserGsonAdapter extends AbstractAssetAdapter {
    @Inject
    public UserGsonAdapter( Provider<LittleUser.Builder> builderFactory ) {
        super( LittleUser.USER_TYPE, builderFactory );
    }
}