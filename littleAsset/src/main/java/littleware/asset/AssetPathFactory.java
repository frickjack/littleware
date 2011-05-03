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

import java.util.*;

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
     * @param path to cleanup
     * @return path without redundant /, /./, and /../
     */
    public String cleanupPath ( String path );

    
    
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
     * @throws AssetException allows generalization in the future
     * @throws InvalidAssetTypeException if path specifies as non name-unique asset-type
     * @throws ParseException on generic parsing error
     */
    public AssetPath createPath ( String pathWithRoot
                                           ) throws AssetException, ParseException;
    
    /**
     * Create an AssetPath rooted at the asset with the given home, type, and name,
     * then traversing s_subroot_path.
     *
     * @param rootName name of the root asset
     * @param rootType asset-type of the root asset
     * @throws InvalidAssetTypeException if n_root_type is not name-unique
     */
    public AssetPath createPath ( String rootName,
                                           AssetType rootType,
                                           String subrootPath
                                         ) throws ParseException, InvalidAssetTypeException, AssetException;
    
    /**
     * Create an AssetPath rooted at the asset with the given id,
     * then traversing s_subroot_path.
     */    
    public AssetPath createPath ( UUID rootId, String subrootPath
                                           ) throws ParseException;
    
    /**
     * Create an AssetPath rooted at the asset with the given id
     */    
    public AssetPath createPath ( UUID rootId
                                           );

}

