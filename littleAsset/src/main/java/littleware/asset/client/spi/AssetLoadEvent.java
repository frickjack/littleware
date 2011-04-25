/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.client.spi;

import littleware.asset.Asset;

/**
 * Event fired by a service that has just loaded
 * an updated view of an asset from the repo - possibly
 * after updating/creating the asset.
 */
public class AssetLoadEvent extends LittleServiceEvent {
    private static final long serialVersionUID = 4242312050123652047L;
    private final Asset loadedAsset;

    public AssetLoadEvent( Object source, Asset loadedAsset ) {
        super( source );
        this.loadedAsset = loadedAsset;
        if ( null == loadedAsset ) {
            // catch this early
            throw new NullPointerException();
        }
    }

    public Asset getAsset() {
        return loadedAsset;
    }
}
