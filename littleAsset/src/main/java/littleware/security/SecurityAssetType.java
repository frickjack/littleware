/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security;

import java.util.*;

import littleware.asset.*;

import littleware.base.UUIDFactory;
import littleware.base.Maybe;

/** 
 * AssetType specializer and bucket for littleware.security
 * based AssetTypes.
 */
public abstract class SecurityAssetType extends AssetType {

    private static final long serialVersionUID = 4444442L;

    protected SecurityAssetType(UUID id, String name) {
        super(id, name);
    }

    
    /** 
     * SERVICE_STUB asset type - with NULL asset specializer 
     * - must be ADMIN to create -regulates access to SessionManager services.
     */
    public static final AssetType SERVICE_STUB = new AssetType(
            UUIDFactory.parseUUID("6AD504ACBB3A4A2CAB5AECE02D8E6706"),
            "littleware.SERVICE_STUB") {
        @Override
        public boolean isAdminToCreate() {
            return true;
        }
    };


}

