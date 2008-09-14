package littleware.asset;

import java.util.UUID;
import java.security.GeneralSecurityException;
import java.rmi.RemoteException;

import littleware.base.BaseException;


/**
 * Simple implementatin of AssetPathByRootId interface.
 */
public class SimpleAssetPathByRootId extends AbstractAssetPath implements AssetPathByRootId {
    private UUID   ou_root = null;
    
    
    public SimpleAssetPathByRootId ( UUID u_root, String s_subroot_path ) {
        super ( "/byid:" + u_root.toString () + "/" + s_subroot_path );
        ou_root = u_root;
    }
    
    
    public UUID getRootId () {
        return ou_root;
    }
            
    public Asset getRoot ( AssetSearchManager m_search 
                                    ) throws BaseException, AssetException, GeneralSecurityException,
        RemoteException
    {
        return m_search.getAsset ( ou_root );
    }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com
