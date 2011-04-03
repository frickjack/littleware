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

import com.google.inject.ImplementedBy;
import littleware.asset.AssetTreeTemplate;

/**
 * Generally want to put new user in standard
 * layout on tree with maybe a user icon or contact children or
 * add to groups, whatever.
 */
@ImplementedBy(SimpleUserTreeBuilder.class)
public interface UserTreeBuilder {
    public UserTreeBuilder user( String value );

    /**
     * Return a template to build user tree -
     * default implementation builds
     *         parent/Users/1stLetter/NewUser
     */
    public AssetTreeTemplate build();
}
