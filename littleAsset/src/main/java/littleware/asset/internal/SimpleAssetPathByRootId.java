/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.internal;

import java.util.UUID;
import littleware.asset.AssetPathByRootId;
import littleware.asset.AssetPathFactory;



/**
 * Simple implementatin of AssetPathByRootId interface.
 */
public class SimpleAssetPathByRootId extends AbstractAssetPath implements AssetPathByRootId {
    private static final long serialVersionUID = -1220806190088603807L;
    private final UUID   rootId;
    
    
    public SimpleAssetPathByRootId ( UUID rootId, String subrootPath, AssetPathFactory pathFactory ) {
        super ( "/byid:" + rootId.toString () + "/" + subrootPath, pathFactory );
        this.rootId = rootId;
    }
    
    
    @Override
    public UUID getRootId () {
        return rootId;
    }
            
}
