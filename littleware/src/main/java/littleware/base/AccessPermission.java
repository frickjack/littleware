/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.base;

import java.security.*;

/**
 * Permission to access protected littleware resources.
 */
public class AccessPermission extends BasicPermission {

    /**
     * Constructor just passes permission spec to superclass
     * as "littleware.db.resource." + s_name
     *
     * @param s_name of resource to access
     */
    public AccessPermission(String s_name) {
        super("littleware.resource." + s_name);
    }
}

