package littleware.asset;

import java.util.logging.Logger;
import littleware.base.UUIDFactory;
import littleware.security.LittleAcl;
import littleware.security.LittleGroup;
import littleware.security.LittlePrincipal;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;


/**
 * Test AssetType property methods
 */
@RunWith( BlockJUnit4ClassRunner.class )
public class AssetTypeTester {
    private static final Logger log = Logger.getLogger( AssetTypeTester.class.getName() );

    final AssetType BOGUS = new AssetType(
            UUIDFactory.parseUUID("7D7B573B-4BF5-4A2F-BDC1-A614935E56AD"),
            "littleware.BOGUS", LittlePrincipal.PRINCIPAL_TYPE ) {};

    /**
     * Just stick this test here rather than make a separate class.
     * Verify that AssetType inheritance sort of works.
     */
    @Test
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
