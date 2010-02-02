/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.validate;

import littleware.asset.AssetBuilder;
import littleware.base.Validator;

/**
 * Validate AssetBuilder properties are in propert state
 * before building an Asset
 */
public interface AssetBuilderValidator {
    public Validator build( final AssetBuilder builder );
}
