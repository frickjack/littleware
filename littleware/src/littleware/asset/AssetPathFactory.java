/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.base.ParseException;

/**
 * Source of AssetPath objects.
 */
public abstract class AssetPathFactory {
    private static Logger olog_generic = Logger.getLogger ( "littleware.asset.AssetPathFactory" );
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
     * Make it easy to swap in subtype implementations later by
     * requiring users to create AssetPathFactory via this method.
     */
    public static AssetPathFactory getFactory () {
        return new SimpleAssetPathFactory ();
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
    public static String cleanupPath ( String s_path ) {
        List<String> v_parts = new ArrayList<String> ();
        olog_generic.log ( Level.FINE, "Processing path: " + s_path );

        for ( StringTokenizer token_slash = new StringTokenizer ( s_path, "/" );
              token_slash.hasMoreTokens ();
              ) {
            String s_token = token_slash.nextToken ();
            olog_generic.log ( Level.FINE, "Processing token: " + s_token );
            if ( s_token.equals ( "." ) ) {
                continue;
            }
            if ( s_token.equals ( ".." ) 
                 && (v_parts.size () > 1)
                 && (! v_parts.get ( v_parts.size () - 1 ).equals( ".." ))
                 ) {
                // remove last part
                olog_generic.log ( Level.FINE, "Got .., removing: " + v_parts.remove ( v_parts.size () - 1 ) );
                continue;
            }
            olog_generic.log ( Level.FINE, "Adding token to token list: " + s_token );
            v_parts.add ( s_token );
        }
        
        String       s_result = "/";
        if ( ! v_parts.isEmpty () ) {
            StringBuilder sb_result = new StringBuilder ( 256 );
            for ( String s_part : v_parts ) {
                sb_result.append ( "/" );
                sb_result.append ( s_part );
            }
            s_result = sb_result.toString ();
        } 
        return s_result;
    }
    
    
    
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
                                         ) throws ParseException, InvalidAssetTypeException;
    
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
}

