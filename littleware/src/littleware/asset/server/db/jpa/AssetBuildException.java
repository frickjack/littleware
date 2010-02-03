/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.server.db.jpa;

import littleware.asset.AssetException;

/**
 * Thrown on failure of AssetEntity to build an asset
 */
public class AssetBuildException extends AssetException {
    private static final long serialVersionUID = -1866208579723750109L;

    public AssetBuildException () {}
    public AssetBuildException ( String sMessage ) {
        super( sMessage );
    }
    public AssetBuildException( String sMessage, Throwable ex ) {
        super( sMessage, ex );
    }
}
