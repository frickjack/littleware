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

import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.*;

import littleware.base.BaseException;
import littleware.base.ParseException;

/**
 * Source of AssetPath objects.
 */
public interface AssetPathFactory {
    /**
     * Prefix for different types of AssetPath
     */
    public enum PathRootPrefix {
        ById {
            @Override
            public String toString () { return "byid:"; }
        },
        ByName {
            @Override
            public String toString () { return "byname:"; }
        },
    }
    
    
    /**
     * Little utility removes redundant /, /./, and /../ from a path.
     * Does not remove /../ that would require removing the first (root)
     * element from the path.
     * So /A/B/../C becomes /A/C,
     * and /A/B/../../C becomes /A/../C.
     *
     * @param s_path to cleanup
     * @return s_path without redundant /, /./, and /../ 
     */
    public String cleanupPath ( String s_path );

    
    
    /**
     * Create an AssetPath referencing the given path.
     * The /ROOT/ part of a path /ROOT/A/B/C specifies the
     * root asset of the AssetPath asset-tree traversal,
     * and the ROOT must obey a syntax for which
     * a handler has been registered to identify the root-asset.  
     * Currently only 
     *       {@link AssetPathByRootId byid:}
     * and 
     *       {@link AssetPathByRootName byname:} ROOT rules exist.
     *
     * @exception NoSuchTypeException on failure to resolve requested AssetType
     * @exception AssetException allows generalization in the future
     * @exception InvalidAssetTypeException if path specifies as non name-unique asset-type
     * @exception ParseException on generic parsing error
     */
    public abstract AssetPath createPath ( String s_path_with_root
                                           ) throws AssetException, ParseException;
    
    /**
     * Create an AssetPath rooted at the asset with the given home, type, and name,
     * then traversing s_subroot_path.
     *
     * @param s_root_name name of the root asset
     * @param n_root_type asset-type of the root asset
     * @exception InvalidAssetTypeException if n_root_type is not name-unique
     */
    public abstract AssetPath createPath ( String s_root_name,
                                           AssetType n_root_type, 
                                           String s_subroot_path
                                         ) throws ParseException, InvalidAssetTypeException, AssetException;
    
    /**
     * Create an AssetPath rooted at the asset with the given id,
     * then traversing s_subroot_path.
     */    
    public abstract AssetPath createPath ( UUID u_root, String s_subroot_path
                                           ) throws ParseException;
    
    /**
     * Create an AssetPath rooted at the asset with the given id
     */    
    public abstract AssetPath createPath ( UUID u_root
                                           );

    /**
     * Generate a path rooted so that there is no backtrack.
     *
     * @see AssetPath#hasRootBacktrack()
     * @return hasRootBacktrack () ? (path rooted without backtrack) : pathIn
     */
    public AssetPath normalizePath ( AssetPath pathIn
                                    ) throws BaseException, GeneralSecurityException,
        RemoteException;

    /**
     * Convert path rooted at path with non-null fromId property
     * to a path rooted at the furthest reachable ancestor
     * 
     * @param pathIn path to convert
     * @return pathIn.getRoot().getFromId() != null ? new rooted bath : pathIn
     * @throws littleware.base.BaseException
     * @throws java.security.GeneralSecurityException
     * @throws java.rmi.RemoteException
     */
    public AssetPath toRootedPath ( AssetPath pathIn
                                    ) throws BaseException, GeneralSecurityException,
        RemoteException;

    /** Shortcut to create rooted path for asset with particular id */
    public AssetPath toRootedPath( UUID uAsset ) throws BaseException, GeneralSecurityException,
        RemoteException;

}

