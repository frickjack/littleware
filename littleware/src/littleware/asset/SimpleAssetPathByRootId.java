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

import java.util.UUID;
import java.security.GeneralSecurityException;
import java.rmi.RemoteException;

import littleware.base.BaseException;
import littleware.base.Maybe;


/**
 * Simple implementatin of AssetPathByRootId interface.
 */
public class SimpleAssetPathByRootId extends AbstractAssetPath implements AssetPathByRootId {
    private static final long serialVersionUID = -1220806190088603807L;
    private UUID   ou_root = null;
    
    
    public SimpleAssetPathByRootId ( UUID u_root, String s_subroot_path ) {
        super ( "/byid:" + u_root.toString () + "/" + s_subroot_path );
        ou_root = u_root;
    }
    
    
    @Override
    public UUID getRootId () {
        return ou_root;
    }
            
    @Override
    public Maybe<Asset> getRoot ( AssetSearchManager m_search
                                    ) throws BaseException, AssetException, GeneralSecurityException,
        RemoteException
    {
        return m_search.getAsset ( ou_root );
    }
}
