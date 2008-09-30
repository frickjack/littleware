package littleware.asset.server.db.test;

import java.sql.*;
import javax.sql.DataSource;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import junit.framework.*;

import littleware.asset.*;
import littleware.asset.server.db.*;
import littleware.db.*;
import littleware.base.*;

/**
 * Tests for DbAssetManager implementations.
 * Gets registered with the littleware.asset.test.PackageTestSuite
 */
public class DbAssetManagerTester extends TestCase {
	private static final Logger  olog_generic = Logger.getLogger ( "littleware.asset.server.db.test.DbAssetManagerTester" );
	private static final UUID    ou_test_id = UUIDFactory.parseUUID ( "00000000000000000000000000000000" );
    
	private final DbAssetManager om_db;
	
	
	/**
	 * Constructor stashes data to run tests against
	 *
	 * @param s_name of test to run
	 * @param m_db manager to test against
	 */
	public DbAssetManagerTester ( String s_name, DbAssetManager m_db
							) {
		super( s_name );
		om_db = m_db;
	}
	
	
	/**
	 * No setup necessary
	 */
	public void setUp ()  {}
	
	/**
	 * No tearDown necessary
	 */
	public void tearDown () {}
	
	/**
	 * Test the load of a well-known test asset
	 */
	public void testLoad () {
		try {
			DbReader<Asset,UUID> db_reader = om_db.makeDbAssetLoader ();
			Asset a_result = db_reader.loadObject ( ou_test_id );
			assertTrue ( "Asset is of proper type", a_result instanceof littleware.security.LittlePrincipal );
			
			// Verify that looking up a non-existent thing does not throw an exceptioon
			Factory<UUID> factory_uuid = UUIDFactory.getFactory ();
			UUID u_test = factory_uuid.create ();
			a_result = db_reader.loadObject ( u_test );
			assertTrue ( "Got null result for nonexistent UUID", null == a_result );
		} catch ( Exception e ) {
			olog_generic.log ( Level.INFO, "Caught unexepcted: " + e + ", " +
							   littleware.base.BaseException.getStackTrace ( e )
							   );
			assertTrue ( "Caught unexpected: " + e, false );
		}
	}
    
    
    /**
     * Test cache-sync stuff - load well known asset,
     * spoof other client modifying that asset, check for update/sync.
     */
    public void testCacheSync () {
        int i_client_id = om_db.getClientId ();
        try {
            int i_client1 = i_client_id + 1;
            int i_client2 = i_client1 + 1;
            
            om_db.setClientId ( i_client1 );
            DbWriter<String> db_clear = om_db.makeDbCacheSyncClearer ();
            db_clear.saveObject ( null );
            
            DbReader<Map<UUID,Asset>,String> db_sync = om_db.makeDbCacheSyncLoader ();
            Map<UUID,Asset>  v_sync = db_sync.loadObject ( null );
            assertTrue ( "Cache clear seems ok", v_sync.isEmpty () );
            
			DbReader<Asset,UUID> db_reader = om_db.makeDbAssetLoader ();
			Asset a_result = db_reader.loadObject ( ou_test_id );
			assertTrue ( "Asset is of proper type", a_result instanceof littleware.security.LittlePrincipal );

			om_db.setClientId ( i_client2 );
            DbWriter<Asset> db_writer = om_db.makeDbAssetSaver ();
            db_writer.saveObject ( a_result );
            
            v_sync = db_sync.loadObject( null );
            Asset a_sync = v_sync.get ( ou_test_id );
            assertTrue ( "Sync detected cross-client update", a_sync != null );
		} catch ( Exception e ) {
			olog_generic.log ( Level.INFO, "Caught unexepcted: " + e + ", " +
							   littleware.base.BaseException.getStackTrace ( e )
							   );
			assertTrue ( "Caught unexpected: " + e, false );
		} finally {
            om_db.setClientId ( i_client_id );
        }
    }  

}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

