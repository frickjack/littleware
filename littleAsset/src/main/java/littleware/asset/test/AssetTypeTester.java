/*
 * Copyright 2011 http://code.google.com/p/littleware/
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.asset.test;

import java.util.logging.Logger;
import littleware.asset.AssetType;
import littleware.asset.GenericAsset;
import littleware.base.UUIDFactory;
import littleware.security.LittleAcl;
import littleware.security.LittleGroup;
import littleware.security.LittlePrincipal;
import littleware.test.LittleTest;

/**
 * Test AssetType property methods
 */
public class AssetTypeTester extends LittleTest {
    private static final Logger log = Logger.getLogger( AssetTypeTester.class.getName() );

    final AssetType BOGUS = new AssetType(
            UUIDFactory.parseUUID("7D7B573B-4BF5-4A2F-BDC1-A614935E56AD"),
            "littleware.BOGUS", LittlePrincipal.PRINCIPAL_TYPE ) {};

    public AssetTypeTester() {
        setName( "testAssetType" );
    }

    /**
     * Just stick this test here rather than make a separate class.
     * Verify that AssetType inheritance sort of works.
     */
    public void testAssetType() {
        assertTrue("BOGUS isA PRINCIPAL",
                BOGUS.isA(LittlePrincipal.PRINCIPAL_TYPE));
        assertTrue("BOGUS != PRINCIPAL",
                !BOGUS.equals(LittlePrincipal.PRINCIPAL_TYPE));
        assertTrue("BOGUS is name unique",
                !BOGUS.isNameUnique());
        assertTrue("BOGUS is not admin-create only",
                !BOGUS.isAdminToCreate()
                );
        assertTrue( "Principal subtypes include bogus", AssetType.getSubtypes( LittlePrincipal.PRINCIPAL_TYPE ).contains( BOGUS ));
        
        assertTrue( "Generic asset is time-stamp cache friendly", GenericAsset.GENERIC.isTStampCache() );
        assertTrue( "Group is not time-stamp cache friendly", ! LittleGroup.GROUP_TYPE.isTStampCache() );
        assertTrue( "ACL is not time-stamp cache friendly", ! LittleAcl.ACL_TYPE.isTStampCache() );
    }
}
