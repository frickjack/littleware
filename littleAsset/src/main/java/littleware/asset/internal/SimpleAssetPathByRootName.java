/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.internal;

import java.security.GeneralSecurityException;
import java.rmi.RemoteException;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.asset.AssetPathByRootName;
import littleware.asset.AssetPathFactory;
import littleware.asset.client.AssetSearchManager;
import littleware.asset.AssetType;
import littleware.asset.InvalidAssetTypeException;
import littleware.asset.LittleHome;

import littleware.base.BaseException;
import littleware.base.Maybe;


/**
 * Simple implementatin of AssetPathByRootName interface.
 */
public class SimpleAssetPathByRootName extends AbstractAssetPath implements AssetPathByRootName {
    private static final long serialVersionUID = -1141969192993296586L;
    private AssetType  on_type = null;
    private String     os_name = null;
    
    
    /**
     * Setup the path.
     *
     * @throws InvalidAssetTypeException unless n_type.isNameUnique()
     */
    public SimpleAssetPathByRootName ( AssetType n_type, 
                                       String s_root_name,
                                       String s_subroot_path,
                                       AssetPathFactory pathFactory
                                       ) throws InvalidAssetTypeException
    {
        super ( ( n_type.equals(LittleHome.HOME_TYPE) ?
                    ("/" + s_root_name) :
                    ("/byname:" + s_root_name + ":type:" + n_type)
                ) + "/" + s_subroot_path,
                pathFactory
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
    @Override
    public AssetType getRootType () {
        return on_type;
    }
    
    @Override
    public String getRootName () {
        return os_name;
    }
    
    @Override
    public Option<Asset> getRoot ( AssetSearchManager m_search
                                    ) throws BaseException, AssetException, GeneralSecurityException,
        RemoteException
    {
        if ( LittleHome.HOME_TYPE.equals ( on_type ) ) {
            return m_search.getByName( os_name, on_type );
        } else {
            return m_search.getByName( os_name, on_type );
        }
    }
    
}

