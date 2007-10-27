package littleware.asset.test;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.security.Principal;

import junit.framework.*;

import littleware.asset.*;
import littleware.base.*;
import littleware.security.SecurityAssetType;


/**
 * Test AssetBuilder
 */
public class AssetBuilderTester extends TestCase {
	private static final Logger olog_generic = Logger.getLogger ( "littleware.asset.test.AssetBuilderTester" );
    
    /** No setup necessary */
    public void setUp () {}
    
    /** No tearDown necessary */
    public void tearDown () {}
    
    /**
     * Just call through to super
     */
    public AssetBuilderTester ( String s_test_name ) {
        super ( s_test_name );
    }
    
    /**
     * Just build some assets, and make sure we get what we expect
     */
    public void testBuild () {
        Factory<UUID>  factory_uuid = UUIDFactory.getFactory ();
        AssetBuilder build_test = new AssetBuilder ().setAssetType ( AssetType.HOME ).
            setName( "root" );
        Asset a_home = build_test.create ();
        Asset a_acl = build_test.setHomeId ( a_home.getObjectId () ).setAssetType ( SecurityAssetType.ACL ).
            setFromId ( a_home.getObjectId () ).setName( "acl" ).create ();
        
        assertTrue ( "name set ok", a_home.getName ().equals ( "root" )
                     && a_acl.getName ().equals ( "acl" )
                     );
        assertTrue ( "objectid set ok", ! a_home.getObjectId ().equals ( a_acl.getObjectId () ) );
        assertTrue ( "homeid set ok", a_acl.getHomeId ().equals ( a_home.getObjectId () ) );
        assertTrue ( "AssetType set ok", a_acl.getAssetType ().equals ( SecurityAssetType.ACL )
                     && a_home.getAssetType().equals ( AssetType.HOME )
                     );

        Asset a_folder = build_test.setAssetType ( AssetType.GENERIC ).setAclId ( a_acl.getObjectId () ).setName( null ).create ();
        assertTrue ( "aclid set ok", a_folder.getAclId ().equals ( a_acl.getObjectId () ) );
        assertTrue ( "fromid set ok", a_folder.getFromId ().equals ( a_home.getObjectId () ) );
        assertTrue ( "null name set to objectid ok", a_folder.getName ().equals ( a_folder.getObjectId ().toString () ) );
        Asset a_link = build_test.setAssetType ( AssetType.LINK ).setToId ( a_folder.getObjectId () ).setName ( "link" ).create ();
        assertTrue ( "toid set ok", a_link.getToId ().equals ( a_folder.getObjectId () ) );
    }
    
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

