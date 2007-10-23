package littleware.asset;

import java.util.*;
import java.security.GeneralSecurityException;
import java.rmi.RemoteException;
import java.rmi.Remote;

import littleware.base.*;
import littleware.security.*;


/**
 * Asset-search interface.  Searches the local server database only.
 * Does not extends Remote so we have the option of
 * sending cilents serializable proxies, but every method
 * does throw RemoteException so this interface is
 * ready for a Remote mixin.
 */
public interface AssetSearchManager extends AssetRetriever, Remote {    
    /**
     * Convenience method - equivalent to: <br />
     *              getAssetsByName ( ... ).getIterator ().next () <br />
     * Handy for asset-types that are name unique.
     *
     *
	 * @param s_name to retrieve
	 * @param n_type must be unique-name type
	 * @return the asset     
     * @exception InavlidAssetTypeException if n_type is not name-unique
     */
    @ReadOnly
    public <T extends Asset> T getByName ( String s_name, AssetType<T> n_type
                                            ) throws BaseException, AssetException,
        GeneralSecurityException, RemoteException;
    
    /**
     * Get the assets along the given asset path.
     * The caller must have read-access to every asset along the path.
     * The traversal fails if it traverses more than 20 assets.
     *
     * @param path_asset to traverse
     * @return map of assets in order traversed from the normalized root
     * @exception GeneralSecurityException if caller does not have read-access
     *             to every asset along the path
     * @exception AssetPathTooLongException if traversal exceeds limit on number of assets
     * @exception LinkLoopException if a loop is detected during automatic link traversal
     */
    public @ReadOnly Map<AssetPath,Asset> getAssetsAlongPath ( AssetPath path_asset
                                                ) throws BaseException, AssetException,
        GeneralSecurityException, RemoteException;
    
    /**
     * Convenience method just retrieves the asset referenced by the
     * last AssetPath returned by getAssetsAlongPath
     *
     * @param path_asset to traverse
     * @return the asset at the end of the path
     * @exception GeneralSecurityException if caller does not have read-access
     *             to every asset along the path
     * @exception AssetPathTooLongException if traversal exceeds limit on number of assets
     * @exception LinkLoopException if a loop is detected during automatic link traversal
     * @exception DanglingLinkException if traversal runs into a link pointing TO nowhere
     */
    public @ReadOnly Asset getAssetAtPath ( AssetPath path_asset
                                  ) throws BaseException, AssetException,
        GeneralSecurityException, RemoteException;
           

	 
	 /**
	  * Get the history of changes on the specified asset going back to the specified date.
	  * Asset must be local to this server's database.
	  *
	  * @param u_id of asset to get history for
	  * @param t_start earliest date to go back to in history search
	  * @param t_end most recent date to go up to in history search
	  * @exception NoSuchThingException if the given asset does not exist in the database
	  * @exception AccessDeniedException if do not CURRENTLY have read-access to the asset
	  * @exception DataAccessException on database access/interaction failure
	  */
	 public @ReadOnly List<Asset> getAssetHistory ( UUID u_id, Date t_start, Date t_end )
		 throws BaseException, AssetException, 
		 GeneralSecurityException, RemoteException;
	 
     /**
      * Get the asset linking FROM the given parent asset and
      * with the given name
      *
      * @param u_from result&apos;s FROM-asset id
      * @param s_name of result asset
      * @exception NoSuchThingException if requested asset does not exist
      */
     public @ReadOnly Asset getAssetFrom ( UUID u_from, String s_name 	
                                           ) throws BaseException, AssetException, 
         GeneralSecurityException, RemoteException;
     
     /**
      * Same as {@link #getAssetFrom(Asset,String) getAssetFrom} except
      * return NULL if the requested asset does not exist.
      *
      * @param u_from result&apos;s FROM-asset id
      * @param s_name of result asset
      */
     public @ReadOnly Asset getAssetFromOrNull ( UUID u_from, String s_name 	
                                           ) throws BaseException, AssetException, 
         GeneralSecurityException, RemoteException;
     
     /**
      * Method for a client to verify the transaction-counts
      * the client has in cache for a set of assets
      *
      * @param v_check mapping from asset id to transaction count to verify
      * @return subset of v_check that is incorrect with correct mapping
      *              from id to transaction-count, or mapping from id
      *              to null if the specified id has been deleted from the asset repository
      */
     public @ReadOnly Map<UUID,Long> checkTransactionCount( Map<UUID,Long> v_check
                                                            ) throws BaseException, RemoteException;
     
     /**
      * Get the links (assets with a_to as their TO-asset)
      * out of the given asset-id of the given type.
      * Caller must have READ-access to the a_to asset.
      *
      * @param u_to asset - result&apos;s TO-asset
      * @param n_type to limit search to - may NOT be null
      * @return ids of children of type n_type linking TO a_to
      * @exception AccessDeniedException if caller does not have read access
      *                to a_source
      * @exception DataAccessException on database access/interaction failure
      * @exception IllegalArgumentExcetion if limit is out of bounds
      * @exception AssetException if limit is too large 
      */
     public @ReadOnly Set<UUID> getAssetIdsTo ( UUID u_to,
                                                         AssetType<? extends Asset> n_type
                                                         ) throws BaseException, AssetException, 
         GeneralSecurityException, RemoteException;
     
     
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

