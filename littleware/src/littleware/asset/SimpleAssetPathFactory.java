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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.UUID;

import littleware.base.BaseException;
import littleware.base.Maybe;
import littleware.base.ParseException;
import littleware.base.UUIDFactory;



/**
 * Source of AssetPath objects.
 */
@Singleton
public class SimpleAssetPathFactory implements AssetPathFactory {
    private static final Logger   olog = Logger.getLogger ( SimpleAssetPathFactory.class.getName() );

    private final AssetSearchManager osearch;


    /** Injecting constructor */
    @Inject
    public SimpleAssetPathFactory (
            AssetSearchManager search
            )
    {
        osearch = search;
    }
        
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
        AssetType    atype = AssetType.HOME;
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
                                       
    
    @Override
    public  AssetPath createPath ( String s_root_name, AssetType n_root_type, 
                                           String s_path
                                           ) throws ParseException, InvalidAssetTypeException
    {
        return new SimpleAssetPathByRootName( n_root_type, s_root_name,
                                          s_path, this
                                          );
    }
    
    @Override
    public  AssetPath createPath ( UUID u_root, String s_path
                                           ) throws ParseException
    {
        return new SimpleAssetPathByRootId( u_root,
                                          s_path, this
                                          );
    }
    
    @Override
    public AssetPath createPath ( UUID u_root ) {
        return new SimpleAssetPathByRootId( u_root, "", this );
    }

    @Override
    public String cleanupPath ( String s_path ) {
        final List<String> v_parts = new ArrayList<String> ();
        olog.log ( Level.FINE, "Processing path: " + s_path );

        for ( StringTokenizer token_slash = new StringTokenizer ( s_path, "/" );
              token_slash.hasMoreTokens ();
              ) {
            String s_token = token_slash.nextToken ();
            olog.log ( Level.FINE, "Processing token: " + s_token );
            if ( s_token.equals ( "." ) ) {
                continue;
            }
            if ( s_token.equals ( ".." )
                 && (v_parts.size () > 1)
                 && (! v_parts.get ( v_parts.size () - 1 ).equals( ".." ))
                 ) {
                // remove last part
                olog.log ( Level.FINE, "Got .., removing: " + v_parts.remove ( v_parts.size () - 1 ) );
                continue;
            }
            olog.log ( Level.FINE, "Adding token to token list: " + s_token );
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

    @Override
    public AssetPath normalizePath ( AssetPath pathIn
                                     ) throws BaseException, AssetException, GeneralSecurityException,
        RemoteException
    {
        String s_normal = pathIn.getSubRootPath();
        if ( s_normal.startsWith ( ".." ) ) { // just in case
            s_normal = "/" + s_normal;
        }
        if ( ! s_normal.startsWith ( "/.." ) ) {
            return pathIn;
        }
        Asset a_root = pathIn.getRoot( osearch ).get();
        for ( ;
              s_normal.startsWith ( "/.." );
              s_normal = s_normal.substring ( 3 )
              ) {
            if ( null == a_root.getFromId () ) {
                throw new DanglingLinkException ( "Unable to normalize path for " + pathIn );
            }
            final Maybe<Asset> maybeParent = osearch.getAsset ( a_root.getFromId () );
            if ( ! maybeParent.isSet() ) {
                throw new DanglingLinkException ( "Unable to normalize path for " + pathIn );
            }
            a_root = maybeParent.get();
        }
        return createPath ( a_root.getId (), s_normal );
    }

    @Override
    public AssetPath toRootedPath ( AssetPath pathIn
                                    ) throws BaseException, GeneralSecurityException,
        RemoteException
    {
        final AssetPath pathNormal = normalizePath( pathIn );
        final Maybe<Asset> maybeRoot = pathNormal.getRoot( osearch );
        if ( ! maybeRoot.isSet() )
        {
            return pathNormal;
        }
        final List<Asset>  vTrail = new ArrayList<Asset>();
        vTrail.add( maybeRoot.get() );
        for( Maybe<Asset> maybeParent = osearch.getAsset( maybeRoot.get().getFromId() );
             maybeParent.isSet();
             maybeParent = osearch.getAsset( maybeParent.get().getFromId() )
                )
        {
            vTrail.add( maybeParent.get() );
        }
        Collections.reverse(vTrail);
        final StringBuilder sbSubrootPath = new StringBuilder();
        boolean bFirst = true;
        for( Asset aPart : vTrail ) {
            if ( bFirst ) {
                // skip the root
                bFirst = false;
                continue;
            }
            sbSubrootPath.append("/" ).append( aPart.getName() );
        }
        sbSubrootPath.append( "/" ).append( pathNormal.getSubRootPath() );
        final Asset aRoot = vTrail.get(0);
        if ( aRoot.getAssetType().isNameUnique() ) {
            return normalizePath( createPath( aRoot.getName(), aRoot.getAssetType(), sbSubrootPath.toString() ) );
        } else {
            return normalizePath( createPath( aRoot.getId(), sbSubrootPath.toString() ) );
        }
    }

    @Override
    public AssetPath toRootedPath(UUID uAsset) throws BaseException, GeneralSecurityException, RemoteException {
        return toRootedPath( createPath( uAsset ) );
    }

}


