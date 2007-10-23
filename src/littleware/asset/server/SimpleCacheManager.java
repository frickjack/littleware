package littleware.asset.server;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.sql.*;
import java.security.*;

import littleware.asset.*;
import littleware.asset.server.db.*;
import littleware.base.*;
import littleware.security.*;
import littleware.db.*;


/**
 * Simple implementation of CacheManager maintains
 * an internal UUID to Asset map and local-only
 * Apache derby database.
 * This class is implemented as a singleton, 
 * and is syncrhonized.
 * If TransactionManager.isInTransaction, then miss on
 * all cache lookups, and defer save ops by passing them
 * to the TransactionManager.deferTillOutOfTransaction() method, since they might
 * finally be rolled back in the end.
 */
public class SimpleCacheManager implements CacheManager {
	private static Logger olog_generic = Logger.getLogger ( "littleware.asset.server.SimpleCacheManager" );
	public final static int OI_MAXSIZE = 100000;
	public final static int OI_MAXSECS = 1000000; // no max age
	
	/**
	 * Need to track null cache-entries separately,
	 * since littleware.base.Cache assumes non-null values.
	 */
	private Set<UUID>            ov_null_entries = Collections.synchronizedSet ( new HashSet<UUID> () );
	private Cache<UUID,Asset>    ocache_asset = new SimpleCache<UUID, Asset> ( OI_MAXSECS, OI_MAXSIZE );
    
    private final  Connection    oconn_cache;
	/** Singleton */
	private static SimpleCacheManager   om_thecache = null;

	/** DbManager action factory - access via getDbManager () method */
	private final DbCacheManager       om_db;
	
    /**
     * Initialize the singleton - called by constructor
     */
    private static synchronized void setTheManager ( SimpleCacheManager m_thecache ) {
        if ( null != om_thecache ) {
            throw new SingletonException ();
        }
        
        om_thecache = m_thecache;
    }
    
	/** 
     * Get the singleton 
     */
	public static SimpleCacheManager getTheManager () { 
        if ( null == om_thecache ) {
            throw new NullPointerException ( "Singleton not initialized" );
        }
        return om_thecache; 
    }
	
	/** 
     * Inject dependencies - setup the singleton
     *
     * @param conn_cache derby connection
     * @param m_dbcache db-manager for cache
     * @exception SingletonException after the 1st time this gets called
     */
	public SimpleCacheManager ( Connection conn_cache, DbCacheManager m_dbcache ) {
        oconn_cache = conn_cache;
        om_db = m_dbcache;
        setTheManager ( this );
    }

	public Cache.Policy getPolicy () { return ocache_asset.getPolicy (); }
	
	public int getMaxSize () { return ocache_asset.getMaxSize (); }
	
	public int getMaxEntryAgeSecs () { return ocache_asset.getMaxEntryAgeSecs (); }
	

	/** 
     * Actually clone()s a copy of a_value, and puts that in the cache.
     * Defers data-save till end of transaction if LittleTransaction.isInTransaction.
     */
	public synchronized Asset put ( final UUID u_key, final Asset a_value ) {
        final LittleTransaction trans_maindb = TransactionManager.getTheThreadTransaction ();
        if ( trans_maindb.isDbUpdating () ) {
            // Defer saving new data till out of transaction
            trans_maindb.deferTillTransactionEnd (
                                                new Runnable () {
                                                    public void run () {
                                                        put ( u_key, a_value );
                                                    }
                                                }
                                                );
            return null;
        } 
		try {
			if ( null == a_value ) {
				ov_null_entries.add ( u_key );
				return ocache_asset.remove ( u_key );
			} else {
				final Asset a_cache = a_value.clone ();
				Whatever.check ( "Key must go with value in asset-cache", a_cache.getObjectId ().equals ( u_key ) );
				
				JdbcDbWriter<Asset> db_writer = om_db.makeDbAssetSaver ();
				db_writer.saveObject ( oconn_cache, a_cache );
				
				ov_null_entries.remove ( u_key );
				return ocache_asset.put ( u_key, a_cache );
			}
		} catch ( SQLException e ) {
			throw new AssertionFailedException ( "Failure updating cache, caught: " + e, e );
		}
	}
	
	/** Actually returns a clone of the value if it's not null and ! LittleTransaction.isInTransaction */
	public Asset get ( UUID u_key ) {
        if ( TransactionManager.getTheThreadTransaction ().isDbUpdating () ) {
            return null;
        }
		Asset a_result = ocache_asset.get ( u_key );
		
		if ( null == a_result ) {
			return null;
		}
		return a_result.clone ();
	}
	

    /**
     * Defers operation if LittleTransaction.isInTransaction
     */
	public synchronized Asset remove ( final UUID u_key ) {
        LittleTransaction trans_maindb = TransactionManager.getTheThreadTransaction ();
        if ( trans_maindb.isDbUpdating () ) {
            trans_maindb.deferTillTransactionEnd (
                                                new Runnable () {
                                                    public void run () {
                                                        remove ( u_key );
                                                    }
                                                }
                                                );
            return null;
        }
		try {
			JdbcDbWriter<UUID> db_writer = om_db.makeDbEraser ();
			db_writer.saveObject ( oconn_cache, u_key );
		} catch ( SQLException e ) {
			throw new AssertionFailedException ( "Failure updating cache, caught: " + e, e );
		}
		
		return ocache_asset.remove ( u_key );
	}
	

	public synchronized void clear () {
		try {
			JdbcDbWriter<UUID> db_writer = om_db.makeDbEraser ();
			db_writer.saveObject ( oconn_cache, null );
		} catch ( SQLException e ) {
			throw new AssertionFailedException ( "Failure updating cache, caught: " + e, e );
		}
		
		ocache_asset.clear ();
        ov_null_entries.clear ();
	}
	
	public int size () {
		return ocache_asset.size ();
	}
	

	public boolean isEmpty () {
		return ocache_asset.isEmpty ();
	}
	

	public Map<UUID,Asset> cacheContents () {
		return ocache_asset.cacheContents ();
	}
	

	public Asset getAsset ( UUID u_id ) throws DataAccessException, AssetException, NoSuchThingException, GeneralSecurityException
	{
		Asset a_result = getAssetOrNull ( u_id );
		if ( null == a_result ) {
			throw new NoSuchThingException ();
		}
		return a_result;
	}

	/** Ignore cycle-cache - somebody else should maintain and check that */
	public Asset getAsset ( UUID u_id, Map<UUID,Asset> v_cycle_cache ) throws DataAccessException, AssetException, NoSuchThingException, GeneralSecurityException
	{
		return getAsset ( u_id );
	}


	/**
	 * Return null if there is a cache entry for this u_id key with a null value.
	 * Throw CacheMissException if there is no entry for this
	 * has a no entry for the u_id key or if LittleTransaction.isInTransaction.
	 *
	 * @param u_id to look for
	 * @return the asset or null if the u_id is in the cache
	 * @exception CacheMissException if the u_id is not in the cache
	 */
	public Asset getAssetOrNull ( UUID u_id ) throws DataAccessException, AssetException, GeneralSecurityException
	{
        if ( TransactionManager.getTheThreadTransaction ().isDbUpdating () ) {
            throw new CacheMissException ( "In transaction" );
        }
		if ( ov_null_entries.contains ( u_id ) ) {
			return null;
		} 
		Asset a_result = this.get ( u_id );   // does a clone()
		if ( null != a_result ) {
			return a_result;
		}
		throw new CacheMissException ( "No entry in cache for: " + u_id );
	}
	
	/** Ignore cycle-cache - somebody else should maintain and check that */
	public Asset getAssetOrNull ( UUID u_id, Map<UUID,Asset> v_cycle_cache ) throws DataAccessException, AssetException, GeneralSecurityException
	{
		return getAssetOrNull ( u_id );
	}

	

	/**
	 * Just loops over id's calling getAssetOrNull().
	 * Throws a CacheMissException if any of the requested ID's are not
	 * in cache.
	 */
	public Set<Asset> getAssets ( Collection<UUID> v_id ) throws DataAccessException, AssetException, GeneralSecurityException
	{
		Set<Asset> v_result = new HashSet<Asset> ();
		Set<UUID>  v_done = new HashSet<UUID> ();
		
		for ( UUID u_id : v_id ) {
			if ( ! v_done.contains ( u_id ) ) {
				Asset a_asset = getAssetOrNull ( u_id );
				if ( null != a_asset ) {
					v_result.add ( a_asset );
				}
			}
		}
		return v_result;
	}
	

    /** Also cache miss if LittleTransaction.isInTransaction */
	public synchronized Map<String,UUID> getHomeAssetIds () throws DataAccessException, AssetException, GeneralSecurityException
	{
        if ( TransactionManager.getTheThreadTransaction ().isDbUpdating () ) {
            throw new CacheMissException ( "In transaction" );
        }        
		try {
			JdbcDbReader<Map<String,UUID>,String> db_reader = om_db.makeDbHomeIdsLoader ();
            if ( null == db_reader ) {
                throw new CacheMissException ();
            }
			return db_reader.loadObject ( oconn_cache, null );
		} catch ( SQLException e ) {
			throw new DataAccessException ( "frickjack: " + e, e );
		}
	}
	
    /** NOOP if LittleTransaction.isInTransaction */
	public synchronized void setHomeAssetIds ( Map<String, UUID> v_home_ids ) 
	{
        if ( TransactionManager.getTheThreadTransaction ().isDbUpdating () ) {
            return;
        }        
		try {
			JdbcDbWriter<Map<String,UUID>> db_writer = om_db.makeDbHomeIdsSaver ();
			db_writer.saveObject ( oconn_cache, v_home_ids );
		} catch ( SQLException e ) {
			Whatever.check ( "Data access failure: " + e, false );
		}
	}
		

	public synchronized Map<String,UUID> getAssetIdsFrom ( UUID u_source,
												  AssetType n_type
												  ) throws DataAccessException, AssetException, GeneralSecurityException
	{
        if ( TransactionManager.getTheThreadTransaction ().isDbUpdating () ) {
            throw new CacheMissException ( "In transaction" );
        }        
		try {
			JdbcDbReader<Map<String,UUID>,String> db_reader = om_db.makeDbAssetIdsFromLoader ( u_source, n_type);
            if ( null == db_reader ) {
                throw new CacheMissException ();
            }
			return db_reader.loadObject ( oconn_cache, null );
		} catch ( SQLException e ) {
			throw new DataAccessException ( "Data access failure", e );
		}
	}
	
	
	public synchronized void setAssetIdsFrom ( UUID u_source,
								  AssetType n_type,
								  Map<String,UUID> v_data
								  )
	{
        if ( TransactionManager.getTheThreadTransaction ().isDbUpdating () ) {
            return;
        }
		try {
			JdbcDbWriter<Map<String,UUID>> db_writer = om_db.makeDbAssetIdsFromSaver ( u_source, n_type);
			db_writer.saveObject ( oconn_cache, v_data );
		} catch ( SQLException e ) {
			Whatever.check ( "Data access failure: " + e, false );
		}
	}
	
	public synchronized Set<UUID> getAssetIdsTo ( UUID u_to,
                                                           AssetType n_type
                                                           ) throws DataAccessException, AssetException, GeneralSecurityException
	{
        if ( TransactionManager.getTheThreadTransaction ().isDbUpdating () ) {
            throw new CacheMissException ( "In transaction" );
        }        
		try {
			JdbcDbReader<Set<UUID>,String> db_reader = om_db.makeDbAssetIdsToLoader ( u_to, n_type);
            if ( null == db_reader ) {
                throw new CacheMissException ();
            }            
			return db_reader.loadObject ( oconn_cache, null );
		} catch ( SQLException e ) {
			throw new DataAccessException ( "Data access failure", e );
		}
	}
	
	
	public synchronized void setAssetIdsTo ( UUID u_to,
                                               AssetType n_type,
                                               Set<UUID> v_data
                                               )
	{
        if ( TransactionManager.getTheThreadTransaction ().isDbUpdating () ) {
            return;
        }
		try {
			JdbcDbWriter<Set<UUID>> db_writer = om_db.makeDbAssetIdsToSaver ( u_to, n_type);
			db_writer.saveObject ( oconn_cache, v_data );
		} catch ( SQLException e ) {
			Whatever.check ( "Data access failure: " + e, false );
		}
	}
	
	
	public String getSourceName () { return "cache"; }
	

    public <T extends Asset> T getByName ( String s_name, AssetType<T> n_type ) throws DataAccessException, 
		AssetException, NoSuchThingException, AccessDeniedException, GeneralSecurityException
    {
        if ( TransactionManager.getTheThreadTransaction ().isDbUpdating () ) {
            throw new CacheMissException ( "In transaction" );
        }        
		try {
			JdbcDbReader<Set<UUID>,String> db_reader = om_db.makeDbAssetsByNameLoader ( s_name, n_type, null );
            if ( null == db_reader ) {
                throw new CacheMissException ();
            }
            
			Set<UUID> v_data = db_reader.loadObject ( oconn_cache, null );
            if ( v_data.isEmpty () ) {
                return null;
            }
			return (T) getAssetOrNull( v_data.iterator ().next () );
		} catch ( SQLException e ) {
			throw new DataAccessException ( "frickjack: " + e, e );
		}
        
    }
	
	
	
	public List<Asset> getAssetHistory ( UUID u_id, java.util.Date t_start, java.util.Date t_end )
		throws NoSuchThingException, AccessDeniedException, GeneralSecurityException, 
		DataAccessException, AssetException
	{
		throw new CacheMissException ();
	}

	public void setAssetsByName ( String s_name, AssetType n_type, UUID u_home, Set<Asset> v_data ) 
	{
        if ( TransactionManager.getTheThreadTransaction ().isDbUpdating () ) {
            return;
        }        
		try {
			JdbcDbWriter<Set<Asset>> db_writer = om_db.makeDbAssetsByNameSaver ( s_name, n_type, u_home );
			db_writer.saveObject ( oconn_cache, v_data );
		} catch ( SQLException e ) {
			olog_generic.log ( Level.WARNING, "Cache update caught unexpected: " + e +
							   ", " + BaseException.getStackTrace ( e )
							   );
			throw new AssertionFailedException ( "Data access failure: " + e, e );
		}
	}
	

    /** Not yet implemented */
    public Asset getAssetFrom ( UUID u_from, String s_name 	
                                          ) throws BaseException, AssetException, 
        GeneralSecurityException
    {
        throw new CacheMissException ();
    }
    
    /** Not yet implemented */
    public Asset getAssetFromOrNull ( UUID u_from, String s_name
                                      ) throws BaseException, AssetException,
        GeneralSecurityException
    {
        throw new CacheMissException ();
    }
    

    /** Not yet implemented */
    public Map<AssetPath,Asset> getAssetsAlongPath ( AssetPath path_asset
                                                     ) throws BaseException, AssetException,
        GeneralSecurityException
    {
        throw new CacheMissException ();
    }
    
    /** Not yet implemented */
    public Asset getAssetAtPath ( AssetPath path_asset
                                  ) throws BaseException, AssetException,
        GeneralSecurityException
    {
        throw new CacheMissException ();
    }
   
    /**
     * Not implemented in CacheManager
     */
    public Map<UUID,Long> checkTransactionCount( Map<UUID,Long> v_check
                                                           ) throws BaseException
    {
        throw new CacheMissException ();
    }
    
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

