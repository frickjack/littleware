/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
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
import java.beans.PropertyChangeListener;


import littleware.base.*;
import littleware.security.*;

/**
 * Asset data-bucket base-class.
 */
public interface Asset extends littleware.base.CacheableObject
{	
    /**
     * Enumeration listing the properties that PropertyChangeListeners
     * can listen for changes on - subtypes may include others.
     * Subtypes will have to add their own property names.
     * Name properties with strings like "PackageName.InternfaceName#PropertyName"
     * to avoid name collision.
     */
    public enum Properties {
        AclId {
            @Override
            public String toString () { return OS_INTERFACE + "AclId"; }
        },
        Comment {
            @Override
            public String toString () { return OS_INTERFACE + "Comment"; }
        },
        Data {
            @Override
            public String toString () { return OS_INTERFACE + "Data"; }
        },
        FromId {
            @Override
            public String toString () { return OS_INTERFACE + "FromId"; }
        },
        ToId {
            @Override
            public String toString () { return OS_INTERFACE + "ToId;"; }
        },
        OwnerId {
            @Override
            public String toString () { return OS_INTERFACE + "OwnerId"; }
        },
        Value {
            @Override
            public String toString () { return OS_INTERFACE + "Value"; }
        },
        StartDate {
            @Override
            public String toString () { return OS_INTERFACE + "StartDate"; }
        },
        EndDate {
            @Override
            public String toString () { return OS_INTERFACE + "EndDate"; }
        };
        
        private static final String  OS_INTERFACE = "littleware.apps.Asset#";
        
    }
    
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
	 *
	 * @return value as an Object - so we can Proxy this interface easily
	 */
	public Float       getValue ();
	
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
    
    /** 
     * Implementors must expose safe clone() call.
     * The clone() is usually just a shallow copy with
     * basic information for server side cacheing.
     * Follow clone() with a sync() call to get an accurate copy.
     */
    public Asset clone();
    
    /**
     * Similar to clone() - copy the data out of the a_copy asset
     * into this asset.  Should be NOOP if this == a_copy_source.  
     *
     * @param a_copy_source to copy data out of
     * @exception InvalidAssetTypeException if a_copy is not compatible with this
     */
    public void sync ( Asset a_copy_source ) throws InvalidAssetTypeException;
    
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
    
    /**
     * Allow observers to listen for property changes on this asset.
     * The internal listener list is not serialized, synced, or cloned.
     *
     * @param listen_props listener that wants informed when a setter gets invoked on this object
     */
    public void addPropertyChangeListener( PropertyChangeListener listen_props );

    /**
     * Allow observers to stop listening for changes
     *
     * @param listen_props to stop sending events to
     */
    public void removePropertyChangeListener( PropertyChangeListener listen_props );
    
}
    

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

