/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.lgo;

import com.google.inject.Inject;
import littleware.asset.AssetType;

/**
 * Create an AssetType.LOCK
 */
public class CreateLockCommand extends CreateFolderCommand.Builder {

    @Inject
    public CreateLockCommand ( CreateFolderCommand.Services services
            ) {
        super( CreateLockCommand.class.getName(), services, AssetType.LOCK );
    }
}
