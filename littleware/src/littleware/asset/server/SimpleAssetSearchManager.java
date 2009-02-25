/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.server;

import com.google.inject.Inject;
import java.rmi.RemoteException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.security.GeneralSecurityException;
import java.sql.*;

import littleware.asset.*;
import littleware.asset.server.db.*;
import littleware.base.*;
import littleware.db.*;


/**
 * Simple implementation of Asset-search interface.  
 */
public class SimpleAssetSearchManager extends LocalAssetRetriever implements AssetSearchManager {
	private static final Logger      olog_generic = Logger.getLogger ( SimpleAssetSearchManager.class.getName() );
    
	private final DbAssetManager     om_db;
	private final CacheManager       om_cache;
	private final TransactionManager omgr_trans;
	
	/**
	 * Constructor stashes DataSource, DbManager, and CacheManager
	 */
    @Inject
	public SimpleAssetSearchManager ( DbAssetManager m_db,
								 CacheManager m_cache,
                                 AssetSpecializerRegistry registry_special,
                                 TransactionManager mgr_trans
                                 )
	{
		super ( m_db, m_cache, registry_special, mgr_trans );
		om_db = m_db;
		om_cache = m_cache;
        omgr_trans = mgr_trans;
	}
	
	
	
    public <T extends Asset> T getByName ( String s_name, AssetType<T> n_type
                                            ) throws BaseException, AssetException,
        GeneralSecurityException, RemoteException
    {
        if ( ! n_type.isNameUnique () ) {
            throw new InvalidAssetTypeException ( "getByName requires name-unique type: " + n_type );
        }
        
        try {
            T a_result = om_cache.getByName ( s_name, n_type );
            if ( null == a_result ) {
                return a_result;
            }
            return (T) secureAndSpecialize ( a_result );
        } catch ( CacheMissException e ) {}
        
        // cache miss
        final Set<Asset> v_load;
        Map<UUID,Asset> v_cycle_cache = omgr_trans.getThreadTransaction ().startDbAccess ();
        try {
            try {
                DbReader<Set<Asset>,String> db_reader = om_db.makeDbAssetsByNameLoader ( s_name, n_type, null );
                
                v_load = db_reader.loadObject ( null );
                om_cache.setAssetsByName ( s_name, n_type, null, v_load );
            } catch ( SQLException e ) {
                olog_generic.log ( Level.SEVERE, "Caught unexpected: " + e );
                throw new DataAccessException ( "Unexpected caught: " + e, e );
            }
            
            if ( v_load.isEmpty () ) {
                return null;
            }
            Asset a_load = v_load.iterator ().next ();
            v_cycle_cache.put ( a_load.getObjectId (), a_load );
            return (T) secureAndSpecialize ( a_load );
        } finally {
            omgr_trans.getThreadTransaction ().endDbAccess ( v_cycle_cache );
        }
    }
        
    

									  

	public List<Asset> getAssetHistory ( UUID u_id, java.util.Date t_start, java.util.Date t_end )
		throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		return null;
	}
	
    
    public SortedMap<AssetPath,Asset> getAssetsAlongPath ( AssetPath path_asset
                                                           ) throws BaseException, AssetException,
        GeneralSecurityException, RemoteException
    {	
        // setup a cycle cache
        Map<UUID,Asset> v_cycle_cache = omgr_trans.getThreadTransaction ().startDbAccess ();
		
        try {
            if ( path_asset.hasRootBacktrack () ) {
                return getAssetsAlongPath ( path_asset.normalizePath( this ) );
            }

            SortedMap<AssetPath,Asset> v_result = null;
            String                     s_path = path_asset.toString ();
            Asset                      a_result = null;

            if ( path_asset.hasParent () ) {
                // else get parent
                v_result = getAssetsAlongPath ( path_asset.getParent () );
                
                if ( v_result.size () > 20 ) {
                    throw new TraverseTooLongException ( "Path traversal (" + path_asset + 
                                                         ") exceeds 20 assets at " + s_path
                                                         );
                }            
                
                Asset   a_parent = v_result.get( v_result.lastKey () );
                String  s_name   = s_path.substring ( s_path.lastIndexOf ( "/" ) + 1 );
                
                if ( s_name.equals ( "@" ) ) {
                    a_result = a_parent.getToAsset ( this );
                } else {
                    a_result = getAssetFrom( a_parent.getObjectId (), s_name );
                }
                
            } else {
                v_result = new TreeMap ();
                a_result = path_asset.getRoot( this );
            } 


            for ( int i_link_count = 0;
                  a_result.getAssetType ().equals ( AssetType.LINK );
                  ++i_link_count
                  ) {
                if ( i_link_count > 5 ) {
                    throw new LinkLimitException ( "Traversal exceeded 5 link limit at " + s_path );
                }
                a_result = a_result.getToAsset( this );
            }

            v_result.put ( path_asset, a_result );        
            return v_result;
        } finally {
            omgr_trans.getThreadTransaction ().endDbAccess ( v_cycle_cache );
        }
    }
    

    public Asset getAssetAtPath ( AssetPath path_asset
                                  ) throws BaseException, AssetException,
        GeneralSecurityException, RemoteException
    {
        SortedMap<AssetPath,Asset> v_path = getAssetsAlongPath ( path_asset );
        return v_path.get ( v_path.lastKey () );
	}

    public Asset getAssetFrom ( UUID u_from, String s_name 	
                                ) throws BaseException, AssetException, 
        GeneralSecurityException, RemoteException
    {
        UUID u_id = getAssetIdsFrom ( u_from, null ).get ( s_name );
        if ( null == u_id ) {
            throw new NoSuchThingException ( "Asset " + u_from + " has no child named " + s_name );
        }
        return getAsset( u_id );
    }
    
    public Asset getAssetFromOrNull ( UUID u_from, String s_name 	
                                ) throws BaseException, AssetException, 
        GeneralSecurityException, RemoteException
    {
        UUID u_id = getAssetIdsFrom ( u_from, null ).get ( s_name );
        if ( null == u_id ) {
            return null;
        }
        return getAssetOrNull( u_id );
    }
    
    /** Need to code this guy up */
    public Map<UUID,Long> checkTransactionCount( Map<UUID,Long> v_check
                                                           ) throws BaseException, RemoteException
    {
        Map<UUID,Asset> v_cache = omgr_trans.getThreadTransaction ().startDbAccess ();
        Map<UUID,Long>  v_result = new HashMap<UUID,Long> ();
        
        try {
            for ( Map.Entry<UUID,Long> entry_x : v_check.entrySet () ) {
                Asset a_check = getAssetOrNullInsecure ( entry_x.getKey () );
                if ( null != a_check ) {
                    // asset exists
                    if ( a_check.getTransactionCount () > 
                         entry_x.getValue ()
                         ) {
                        // client is out of date
                        v_result.put ( a_check.getObjectId (),
                                       a_check.getTransactionCount ()
                                       );
                        olog_generic.log ( Level.FINE, "Transaction count missync for: " + a_check );
                    } else {
                        olog_generic.log ( Level.FINE, "Transaction count ok for: " + a_check );
                    }
                } else { // asset does not exist
                    v_result.put ( entry_x.getKey (), null );
                }
            }
        } finally {
            omgr_trans.getThreadTransaction ().endDbAccess ( v_cache );
        }
        return v_result;
    }
    
    
    public Set<UUID> getAssetIdsTo ( UUID u_to,
                                      AssetType<? extends Asset> n_type
                                      ) throws BaseException, AssetException, 
        GeneralSecurityException, RemoteException
    {
        Set<UUID> v_result = null;

		try {
			return om_cache.getAssetIdsTo ( u_to, n_type );
		} catch ( CacheMissException e ) {
            olog_generic.log ( Level.FINE, "Cache miss: " + u_to + ", " + n_type );
        }
		
		// cache miss
		try {
			DbReader<Set<UUID>,String> sql_reader = om_db.makeDbAssetIdsToLoader ( u_to, n_type );
			v_result = sql_reader.loadObject ( null );
		} catch ( SQLException e ) {
			throw new DataAccessException ( "Caught unexpected: " + e );
		}
		
		om_cache.setAssetIdsTo ( u_to, n_type, v_result );
		return v_result;
    }        
    
}

