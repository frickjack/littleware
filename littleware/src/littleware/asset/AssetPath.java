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

import java.security.GeneralSecurityException;
import java.rmi.RemoteException;

import littleware.base.BaseException;
import littleware.base.Maybe;


/**
 * AssetPath associates a String path relative to an AssetRoot
 * defining a traversal along the Asset graph.
 * An asset path resembles a UNIX-style file path with /  separating
 * the names of each asset along the path from each other.
 * Each asset along the path separated by / links FROM the previous asset along the path,
 * so that getAssetByPath( u_root, "A/B/C" );
 * returns an asset C linking FROM asset B linking FROM asset A linking FROM u_root.
 * A @ in the path indicates that the path traverses along the TO link of
 * the preceding asset, so that getAssetByPath ( u_root, "A/B@/C" );
 * returns an asset C linking FROM some unnamed asset that is linked TO by asset B.
 * The @ allows us to easily use a path to traverse links in our asset.
 * Paths like "A/B/@", "/A/@/@/@/@/B/C", "/@/@/@/A/B" behave as expected.
 * If a path "A/B/C" traverses an asset B that 
 * is of type AssetType.LINK, then the traversal transparently attempts to evaluate
 * "A/B@/C"; if "A/B@" references an asset X 
 * of type AssetType.LINK, then the traversal progresses to, "A/B/@/@/C", and so on 
 * until a path is found or the search fails.  
 * Automatic link traversal applies to any element 
 * in the path including the last.
 * Every AssetPath is cleaned at construction to remove redundant /, /./,
 * and /../ tokens (see {@link AssetPathFactory#cleanupPath() AssetPathFactory.cleanupPath()}),
 * but the path may still contain unresolved /../ elements after the ROOT
 * (see {@link #normalizePath( AssetSearchManager ) normalizePath}).
 * Different subtypes of AssetPath may allow different specifications of the /ROOT of a path.
 */
public interface AssetPath extends java.io.Serializable, Comparable<AssetPath>, Cloneable {    
    /**
     * Does the path start with /ROOT(/..)+ ?
     */
    public boolean hasRootBacktrack ();
    
    
    /**
     * Retrieve the asset at the root of the path if it exists
     */
    public Maybe<Asset> getRoot ( AssetSearchManager m_search
                           ) throws BaseException, AssetException, GeneralSecurityException,
        RemoteException;
    
    /**
     * Get the portion of the path that comes after the root
     *
     * @return /A/B/C if this path is /ROOT/A/B/C, / if this path is /ROOT
     */
    public String getSubRootPath ();

    /**
     * Get portion of path after rightmost /
     *
     * @return C if this path is /ROOT/A/B/C, ROOT if this path is /ROOT
     */
    public String getBasename();
    
    /**
     * Retrieve the asset referenced by this path.
     * 
     * @param m_search to traverse the asset tree with
     * @see AssetSearchManager.getAssetAtPath( AssetPath, boolean )
     */
    public Maybe<Asset> getAsset ( AssetSearchManager m_search
                            ) throws BaseException, AssetException, GeneralSecurityException,
        RemoteException;
    
    
        
    
    /**
     * Return (this.getParent().equals ( this )).
     * This does not imply that getRoot().equals( getAsset () ),
     * because the path may have form ROOT/.. - where the ../
     * references the asset the ROOT links FROM.
     *
     * @see #hasRootBacktrack()
     */
    public boolean hasParent ();
    
    /**
     * Get the parent of this path.  The parent of /A/B is /A.
     * The parent of ^/ROOT(/..)* is itself.
     */
    public AssetPath getParent ();
    
    /** Covariant return-type clone */
    public AssetPath clone ();
    
}
