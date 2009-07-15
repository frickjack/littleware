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

import java.rmi.RemoteException;
import java.util.*;
import java.security.acl.Owner;
import java.security.GeneralSecurityException;
import littleware.base.BaseException;
import littleware.base.CacheableObject;
import littleware.base.LittleBean;
import littleware.security.LittleAcl;
import littleware.security.LittleUser;


/**
 * Asset data-bucket base-class.
 * A typical littleware application arranges
 * assets into a tree-like graph rooted under
 * the application's "home" asset.
 * The application arranges assets into different
 * subtrees to categorize the assets in different ways.
 * For example, a request-tracker application might
 * have a very simple structure.
 *     /Application/InBox/
 *     /Application/OutBox/
 * where /Application is a 'home' type asset (see littleware.asset.AssetType),
 * InBox and OutBox are 'generic' assets, and InBox and OutBox
 * have multiple 'request' type asset children.
 */
public interface Asset extends CacheableObject, LittleBean
{	    
	public String      getName ();
	/** Id of user that created this asset */
	public UUID        getCreatorId ();
	/** Id of user that last updated this asset */
	public UUID        getLastUpdaterId ();
	public UUID        getAclId ();
	public AssetType<? extends Asset>   getAssetType ();
	/** Each asset has a comment attached to it */
	public String      getComment ();
	/**
	 * Get comment/log-message associated with the last update
	 * to this asset.
	 */
	public String      getLastUpdate ();
	public String      getData ();
	
	/**
	 * Source of directed link this asset represents - may be null
	 */
	public UUID        getFromId ();
	/**
	 * Destination of directed link - may be null
	 */
	public UUID        getToId ();
	/**
	 * Id of home-asset this asset is associated with - should never be null
	 */
	public UUID        getHomeId ();
	/**
	 * Id of Principal that owns this asset - may be null.
	 * Members of
	 * the littewlare.admin group are also implicit owners
	 * of every object.
	 */
	public UUID        getOwnerId ();
	
	
	/**
	 * Convenience method returns the Home-asset corresponding to this asset's home-id.
	 *
	 * @param m_retriever to retrive home-asset with
	 * @return asset or null if id is null - result may be cached for multiple calls
	 */
	public Asset getHome ( AssetRetriever m_retriever ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException;
	
	/**
	 * Convenience method returns the Owner corresponding to this asset's owner-id.
     * If owner-id is null, then members of the littleware ADMIN group still have
     * owner privileges, so a non-null owner still gets returned.
	 *
	 * @param m_retriever to retrieve owner-principal with
	 * @return owner  - result may be cached for multiple calls
	 */
	public Owner getOwner ( AssetRetriever m_retriever ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException;
	
	/**
	 * Convenience method returns the Acl corresponding to this asset's acl-id
	 *
	 * @param m_retriever to retrieve ACL with
	 * @return acl or null if acl-id is null - result may be cached for multiple calls
	 */
	public LittleAcl getAcl ( AssetRetriever m_retriever ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException;
	/**
	 * Convenience method returns the user that created this asset
	 *
	 * @param m_retriever to retrieve user with
	 * @return user or null if id is null - result may be cached for multiple calls
	 */
	public LittleUser getCreator ( AssetRetriever m_retriever ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException;
	/**
	 * Convenience method returns the user that last updated this asset
	 *
	 * @param m_retriever to retrieve user with
	 * @return user or null if id is null - result may be cached for multiple calls
	 */
	public LittleUser getLastUpdater ( AssetRetriever m_retriever ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException;
	/**
	 * Convenience method returns the asset this asset links from or null
	 *
	 * @param m_retriever to retrieve asset with
	 * @return asset of appropriate type or null - result may be cached
	 */
	public Asset getFromAsset ( AssetRetriever m_retriever ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException;
	
	/**
	 * Convenience method returns the asset this asset links to or null
	 *
	 * @param m_retriever to retrieve asset with
	 * @return asset of appropriate type or null - result may be cached
	 */
	public Asset getToAsset ( AssetRetriever m_retriever ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException;
	
	/**
	 * Asset may have a date-range associated with it 
	 */
	public Date        getStartDate ();
	/**
	 * Asset may have a date-range associated with it 
	 */
	public Date        getEndDate ();
	public Date        getCreateDate ();
	public Date        getLastUpdateDate ();
	/**
	 * May only be accurate the hour or whatever depending on the implementation
	 */
	public Date        getLastAccessDate () ;
	
	/**
	 * Asset may have a float value associated with it
	 * interpreted differently for different asset types
     * (priority, cost, whatever).
	 *
	 * @return value as an Object - so we can Proxy this interface easily
	 */
	public Float       getValue ();

    /**
     * It's very common for asset pipelines to want to put
     * assets into one of several states.
     * Subtypes should generally map a state to an enumeration.
     *
     * @return integer asset state
     */
    public Integer     getState();
	
	/** 
	 * Name may not contain / 
	 *
	 * @exception AssetException if given an illegal name
     */
	public void        setName ( String s_name ) throws IllegalArgumentException;
	
	/** Set the id - flush the getX() cache result */
	public void        setCreatorId ( UUID u_creator );
	/** Set the id - flush the getX() cache result */
	public void        setLastUpdaterId ( UUID u_last_updater );
	/** Set the id - flush the getX() cache result */
	public void        setAclId ( UUID u_acl );
	/** Create a new owner object with u_owner as the only non-admin member - flush getOwner cache */
	public void        setOwnerId ( UUID u_owner ) ;
	public void        setAssetType ( AssetType<? extends Asset> n_type );
	public void        setComment ( String s_comment );
	public void        setLastUpdate ( String s_last_update );
	
	/**
	 * Set the data blob attached to this asset.
	 *
	 * @exception ParseException if data is in invalid format
	 *                for asset type, or if length exceeds 1024 characters
	 */
	public void        setData ( String s_data ) throws BaseException;
	
	public void        setHomeId ( UUID u_home );
	public void        setFromId ( UUID u_from );
	public void        setToId ( UUID u_to );
	
	public void        setStartDate ( Date t_start );
	public void        setEndDate ( Date t_end );
	public void        setCreateDate ( Date t_create_date );
	public void        setLastUpdateDate ( Date t_update_date ); 
	public void        setLastAccessDate ( Date t_access_date );
	
	public void        setValue ( float f_value );
    public void        setState ( int iState );

    /**
     * Property indicates whether this object is in sync
     * with the backend database to the best of the environment's knowlege,
     * or if this object has changes that make it "dirty".
     */
    public boolean isDirty();
    public void setDirty( boolean bDirty );

    
    /** 
     * Implementors must expose safe clone() call.
     * The clone() is usually just a shallow copy with
     * basic information for server side cacheing.
     * Follow clone() with a sync() call to get an accurate copy.
     */
    @Override
    public Asset clone();
    
    /**
     * Similar to clone() - copy the data out of the a_copy asset
     * into this asset.  Should be NOOP if this == a_copy_source.  
     *
     * @param a_copy_source to copy data out of
     * @exception IllegalArgumentException of a_copy_source is not of same type as this
     */
    public void sync ( Asset a_copy_source );
    
    /**
     * Convenience method - equivalent to:
     *     this.sync( m_asset.saveAsset ( this, s_update_comment ) )
     */
    public void save ( AssetManager m_asset, String s_update_comment
                       ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException;
    
    /**
     * Convenience method - equivalent to:
     *     this.sync( m_retriever.getAsset( this.getObjectId () ) )
     */
    public void sync ( AssetRetriever m_retriever 
                       ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException;
    
    
    /** Cast this to the specified asset type ... little safer than simple cast */
    public <T extends Asset> T narrow( Class<T> type );

    public <T extends Asset> T narrow();
}
    
