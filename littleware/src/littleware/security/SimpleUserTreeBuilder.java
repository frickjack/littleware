/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.security;

import littleware.asset.AssetTreeTemplate;

/**
 * Simple UserTreeBuilder implementation sets up tree-template
 * with format parent/Users/1st-letter/user
 */
public class SimpleUserTreeBuilder implements UserTreeBuilder {
    private String user;

    @Override
    public UserTreeBuilder user(String value) {
        if ( ! value.matches( "^\\w+$" ) ) {
            throw new IllegalArgumentException( "Illegal username: " + value );
        }
        user = value;
        return this;
    }

    @Override
    public AssetTreeTemplate build() {
        return new AssetTreeTemplate( "Users",
                new AssetTreeTemplate( user.toUpperCase().substring(0,1),
                    new AssetTreeTemplate( user, SecurityAssetType.USER )
                    ));
    }

}
