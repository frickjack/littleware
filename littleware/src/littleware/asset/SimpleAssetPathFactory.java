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
    private static Logger   olog_generic = Logger.getLogger ( "littleware.asset.SimpleAssetPathFactory" );
    
    /** Do nothing constructor */
    public SimpleAssetPathFactory () {}
        
    @Override
    public  AssetPath createPath ( String s_path_with_root
                                   ) throws AssetException, ParseException {
        if ( ! s_path_with_root.startsWith ( "/" ) ) {
            throw new ParseException ( "Asset-path must start with /: " + s_path_with_root );
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
        
        if ( s_root.startsWith ( AssetPathFactory.PathRootPrefix.ById.toString () ) ) {
            UUID u_root = UUIDFactory.parseUUID ( s_root.substring ( AssetPathFactory.PathRootPrefix.ById.toString ().length () ) );
            return createPath ( u_root, s_subroot_path );
        } else if ( s_root.startsWith ( AssetPathFactory.PathRootPrefix.ByName.toString () ) ) {
            int    i_name = AssetPathFactory.PathRootPrefix.ByName.toString ().length ();
            int    i_type = s_root.indexOf ( ":type:" );
            if ( i_type <= i_name ) {
                throw new ParseException ( "Illegal asset-type root specification: " + s_root );
            }

            String s_name = s_root.substring( i_name, i_type );
            String s_type = s_root.substring( i_type + ":type:".length (), s_root.length () );            
            
            return createPath ( s_name, AssetType.getMember( s_type ), s_subroot_path );
        }
        throw new ParseException ( "Unable to parse root: " + s_root );            
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


// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com
