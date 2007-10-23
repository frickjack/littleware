package littleware.asset.server.db.postgres;

import java.util.*;
import java.sql.SQLException;
import javax.sql.DataSource;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.asset.*;
import littleware.asset.server.db.*;
import littleware.db.*;
import littleware.base.AssertionFailedException;

/**
 * Postgres RDBMS DbAssetManager implementation
 */
public class DbAssetPostgresManager implements DbAssetManager {
    private static final Logger olog_generic = Logger.getLogger ( "littleware.asset.server.db.DbAssetPostgresManager" );
    private  int   oi_client_id = 1;
    
    /**
     * Inject DataSource dependency
     */
    public DbAssetPostgresManager ( int i_client_id ) {
        oi_client_id = i_client_id;
    }
    
	public DbWriter<Asset> makeDbAssetSaver ()
	{
		return new DbAssetSaver ( getClientId () );
	}
	
	public DbReader<Asset,UUID> makeDbAssetLoader () {
		return new DbAssetLoader ( getClientId () );
	}
	
	public DbWriter<Asset> makeDbAssetDeleter () {
		return new DbAssetDeleter ( getClientId () );
	}
	
	public DbReader<Map<String,UUID>,String> makeDbHomeIdLoader () {
		return new DbHomeIdLoader ( getClientId () );
	}	
	
	public DbReader<Map<String,UUID>,String> makeDbAssetIdsFromLoader ( UUID u_from, AssetType n_child_type )
	{
		return new DbChildIdLoader ( u_from, n_child_type, getClientId () );
	}
	
	public DbReader<Set<Asset>,String> makeDbAssetsByNameLoader ( String s_name, AssetType n_type, UUID u_home )
	{
		return new DbAssetsByNameLoader ( s_name, n_type, u_home, getClientId () );
	}

    
    public DbWriter<String> makeDbCacheSyncClearer ()
    {
        return new DbCacheSyncClearer ( getClientId () );
    }
    
    public DbReader<Map<UUID,Asset>,String> makeDbCacheSyncLoader ()
    {
        return new DbCacheSyncLoader ( getClientId () );
    }
    
    
    
    private static boolean ob_cachesync = false;
    
    
    
    public void launchCacheSyncThread ( 
                                        final CacheManager m_cache 
                                        ) throws SQLException
    {
        if ( ob_cachesync ) {
            throw new AssertionFailedException ( "Cache sync relaunch attempted" );
        }
                
        DbWriter<String>  db_clear = makeDbCacheSyncClearer ();
        
        // Clear out old data
        db_clear.saveObject ( null );
        // Setup a thread to keep us in sync
        Runnable  run_sync = new Runnable () {
            public void run () {
                final DbReader<Map<UUID,Asset>,String> db_sync = makeDbCacheSyncLoader ();
                while ( true ) {
                    try {
                        Map<UUID,Asset> v_data = db_sync.loadObject( null );
                        for ( Map.Entry<UUID,Asset> x_entry : v_data.entrySet () ) {
                            Asset a_update = x_entry.getValue ();
                            if ( null != a_update ) {
                                // Verify transaction
                                Asset a_old = (Asset) m_cache.get ( x_entry.getKey () );
                                if ( (a_old == null)
                                     || (a_update.getTransactionCount () >= a_old.getTransactionCount ())
                                     ) {
                                    m_cache.put ( x_entry.getKey (), a_update );
                                } else {
                                    olog_generic.log ( Level.INFO, "Sync thread not updating cache for asset: " + a_old );
                                }
                            } else {
                                m_cache.remove ( x_entry.getKey () );
                            }
                        }
                    } catch ( Exception e ) {
                        olog_generic.log ( Level.WARNING, "Sync thread failed to sync cache, caught: " + e, e );
                    }
                    try {
                        Thread.sleep ( 200 );
                    } catch ( InterruptedException e ) {
                        olog_generic.log ( Level.INFO, "Ignoring InterruptedException: " + e );
                    }
                }
            }
        };

        new Thread ( run_sync ).start ();
        ob_cachesync = true;
    }
    

    
    public int getClientId () {
        return oi_client_id;
    }
    

    public void setClientId ( int i_id ) {
        oi_client_id = i_id;
    }

    public DbReader<Set<UUID>,String> makeDbAssetIdsToLoader ( UUID u_to, AssetType n_type )
    {
        return new DbAssetIdsToLoader ( u_to, n_type, getClientId () );
    }
        

}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

