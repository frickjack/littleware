/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset;

import littleware.asset.AssetPath;
import littleware.asset.AssetType;



/**
 * Specialization of AssetPath that identifies its root
 * by a (Asset-type, Asset-name) tuple, where the type must
 * enforce name uniqueness.
 * <p>
 * The root format is: <br />
 *          /byname:name:type:typename/ <br />.
 * For example - here is the path to the asset holding the default
 * contact information for user pasquini in the asset tree under the littleware.web_home
 * home asset: <br />
 *         /byname:pasquini:type:littleware.USER/contact/default <br />
 * </p>
 */
public interface AssetPathByRootName extends AssetPath {    
    /**
     * Get the name of the asset at the root of this path
     */
    public String getRootName ();
    
    /**
     * Get the type of the asset at the root of this path
     */
    public AssetType getRootType ();
}

