package littleware.asset;

import java.util.UUID;
import java.security.GeneralSecurityException;
import java.rmi.RemoteException;

import littleware.base.BaseException;


/**
 * Simple implementatin of AssetPathByRootName interface.
 */
public class SimpleAssetPathByRootName extends AbstractAssetPath implements AssetPathByRootName {
    private AssetType  on_type = null;
    private String     os_name = null;
    
    
    /**
     * Setup the path.
     *
     * @exception InvalidAssetTypeException unless n_type.isNameUnique()
     */
    public SimpleAssetPathByRootName ( AssetType n_type, 
                                       String s_root_name,
                                       String s_subroot_path ) throws InvalidAssetTypeException
    {
        super ( "/byname:" + s_root_name +
                ":type:" + n_type +
                s_subroot_path
                );

        on_type = n_type;
        os_name = s_root_name;
        if ( ! n_type.isNameUnique () ) {
            throw new InvalidAssetTypeException ( "Asset type not name unique: " + n_type );
        }
    }
    
    /**
     * Get the AssetType of the root asset
     */
    public AssetType getRootType () {
        return on_type;
    }
    
    public String getRootName () {
        return os_name;
    }
    
    public Asset getRoot ( AssetSearchManager m_search 
                                    ) throws BaseException, AssetException, GeneralSecurityException,
        RemoteException
    {
        if ( AssetType.HOME.equals ( on_type ) ) {
            return m_search.getByName( os_name, on_type );
        } else {
            return m_search.getByName( os_name, on_type );
        }
    }
    
}


// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com
