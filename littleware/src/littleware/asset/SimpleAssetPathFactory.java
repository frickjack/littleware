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

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.UUID;

import littleware.base.ParseException;
import littleware.base.UUIDFactory;


/**
 * Source of AssetPath objects.
 */
public class SimpleAssetPathFactory extends AssetPathFactory {    
    private static Logger   olog = Logger.getLogger ( "littleware.asset.SimpleAssetPathFactory" );
    
    /** Do nothing constructor */
    public SimpleAssetPathFactory () {}
        
    @Override
    public  AssetPath createPath ( String s_path_in
                                   ) throws AssetException,ParseException {
        String s_path_with_root = s_path_in;
        if ( ! s_path_with_root.startsWith ( "/" ) ) {
            s_path_with_root = "/" + s_path_in;
        }
        int i_first_slash = s_path_with_root.indexOf ( "/", 1 );
        String s_root = null;
        String s_subroot_path = null;
        
        if ( i_first_slash > 0 ) {
            // Then there's a path part
            s_root = s_path_with_root.substring( 1, i_first_slash );
            s_subroot_path = s_path_with_root.substring( i_first_slash );
        } else {
            s_root = s_path_with_root.substring( 1 );
            s_subroot_path = "";
        }

        // byname AssetPath - default type littleware.HOME, default name s_root
        String s_name = s_root;
        AssetType<?>  atype = AssetType.HOME;
        final int    i_type = s_root.indexOf ( ":type:" );

        if (s_root.startsWith(AssetPathFactory.PathRootPrefix.ById.toString())) {
            try {
                UUID u_root = UUIDFactory.parseUUID(s_root.substring(AssetPathFactory.PathRootPrefix.ById.toString().length()));
                return createPath(u_root, s_subroot_path);
            } catch ( IllegalArgumentException ex ) {
                throw new ParseException( "Invalid uuid: " + s_root, ex );
            }
        } else if (s_root.startsWith(AssetPathFactory.PathRootPrefix.ByName.toString())) {
            // user specified name
            int i_name = AssetPathFactory.PathRootPrefix.ByName.toString().length();

            if (i_type < 0) {
                s_name = s_root.substring(i_name);
            } else {
                s_name = s_root.substring(i_name, i_type);
                atype = AssetType.getMember(s_root.substring(i_type + ":type:".length()));
            }
        } else if (i_type > 0) {
            // user did not specify name, but specified a type
            atype = AssetType.getMember(s_root.substring(i_type + ":type:".length()));
        } else {
            // user specified nothing
            // check to make sure the name is not a UUID
            try {
                return createPath(UUIDFactory.parseUUID(s_name), s_subroot_path);
            } catch (IllegalArgumentException ex) {
                olog.log(Level.FINE, "Unspecified path is not a UUID path");
            }
        }
        return createPath(s_name, atype, s_subroot_path);
   }
                                       
    
    public  AssetPath createPath ( String s_root_name, AssetType n_root_type, 
                                           String s_path
                                           ) throws ParseException, InvalidAssetTypeException
    {
        return new SimpleAssetPathByRootName( n_root_type, s_root_name,
                                          s_path
                                          );
    }
    
    public  AssetPath createPath ( UUID u_root, String s_path
                                           ) throws ParseException
    {
        return new SimpleAssetPathByRootId( u_root,
                                          s_path
                                          );
    }
    
    public AssetPath createPath ( UUID u_root ) {
        return new SimpleAssetPathByRootId( u_root, "" );
    }
}


