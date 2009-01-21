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

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.rmi.RemoteException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.security.acl.Owner;
import java.security.GeneralSecurityException;

import littleware.base.*;
import littleware.security.*;

/**
 * Asset data-bucket base-class.
 */
public class SimpleAsset extends SimpleCacheableObject implements Asset, java.io.Serializable 
{
	private static final     Factory<UUID>  ofactory_uuid = UUIDFactory.getFactory ();
	private static final     Logger         olog_generic = Logger.getLogger ( "littleware.asset.SimpleAsset" );
	
	private UUID        ou_creator = null;
	private UUID        ou_last_updater = null;
	private UUID        ou_acl = null;
	private AssetType   on_type = AssetType.GENERIC;
	private String      os_comment = "";
	private String      os_last_update = "";
	private String      os_name = "never_initialized";
	private float       of_value = 0;
	private UUID        ou_owner = null;
	private String      os_data = "";
	
	private UUID        ou_home = null;
	private UUID        ou_from = null;
	private UUID        ou_to   = null;
	
	private Date        ot_start = null;
	private Date        ot_end   = null;
	
	private Date        ot_create_date = new Date ();
	private Date        ot_update_date = new Date ();
	private Date        ot_access_date = new Date ();
	
	/** Supporting java serialization */
	private static final long serialVersionUID = 42L;
    /** Limit on the size of the data block */
    public static final int OI_DATA_LIMIT = 1024;
    
    private transient PropertyChangeSupport    osupport_props = new PropertyChangeSupport ( this );
    
    public void addPropertyChangeListener(
                                          PropertyChangeListener listen_props )
    {
        osupport_props.addPropertyChangeListener( listen_props );
    }
    public void removePropertyChangeListener(
                                             PropertyChangeListener listen_props )
    {
        osupport_props.removePropertyChangeListener( listen_props );
    }
    
    /**
     * Give subtypes access to the PropertyChangeSupport
     */
    protected PropertyChangeSupport getPropertyChangeSupport () {
        return osupport_props;
    }
	
	/**
	 * Constructor does some basic initialization.
	 * Sets type to AssetType.GENERIC, object-id to new id.
	 */
	public SimpleAsset () {
		try {
			this.setObjectId ( ofactory_uuid.create () );
		} catch ( FactoryException e ) {
			olog_generic.log ( Level.FINE, "New SimpleAsset left with minimum initialization, caught: " + e );
		}
	}
	
	public String      getName () { return os_name; }
	public UUID        getCreatorId () { return ou_creator; }
	public UUID        getLastUpdaterId () { return ou_last_updater; }
	public UUID        getAclId () { return ou_acl; }
	public AssetType   getAssetType () { return on_type; }
	public String      getComment () { return os_comment; }
	public String      getLastUpdate () { return os_last_update; }
	public String      getData () { return os_data; }
	
	public UUID        getFromId () { return ou_from; }
	public UUID        getToId () { return ou_to; }
	public UUID        getHomeId () { return ou_home; }
	public UUID        getOwnerId () { return ou_owner; }
	
	public Date        getStartDate () { return ot_start; }
	public Date        getEndDate () { return ot_end; }
	public Date        getCreateDate () { return ot_create_date; }
	public Date        getLastUpdateDate () { return ot_update_date; }
	public Date        getLastAccessDate () { return ot_access_date; }
	
	public Float       getValue () { return of_value; }
	

	public Asset getHome ( AssetRetriever m_retriever ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException {
		if ( null == ou_home ) {
			return null;
		}
		return m_retriever.getAsset ( ou_home );
	}

	public Owner getOwner ( AssetRetriever m_retriever ) 
		throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		if ( null == ou_owner ) {
            return new SimpleOwner ( 
                                     (LittleGroup) m_retriever.getAsset ( AccountManager.UUID_ADMIN_GROUP )
                                     );
		}
        
		return new SimpleOwner ( (LittleUser) m_retriever.getAsset ( ou_owner ),
                                 (LittleGroup) m_retriever.getAsset ( AccountManager.UUID_ADMIN_GROUP )
                                 );
	}

	public LittleAcl getAcl ( AssetRetriever m_retriever ) 
		throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		if ( null == ou_acl ) {
			return null;
		}
		return (LittleAcl) m_retriever.getAsset ( ou_acl );
	}

	public LittleUser getCreator ( AssetRetriever m_retriever ) 
		throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		if ( null == ou_creator ) {
			return null;
		}
		return (LittleUser) m_retriever.getAsset ( ou_creator );
	}

	public LittleUser getLastUpdater ( AssetRetriever m_retriever ) 
	    throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		if ( null == ou_last_updater ) {
			return null;
		}
		return (LittleUser) m_retriever.getAsset ( ou_last_updater );
	}

	public Asset getFromAsset ( AssetRetriever m_retriever ) 
		throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		if ( null == ou_from ) {
			return null;
		}
		return m_retriever.getAsset ( ou_from );
	}

	public Asset getToAsset ( AssetRetriever m_retriever ) 
	    throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		if ( null == ou_to ) {
			return null;
		}
		return m_retriever.getAsset ( ou_to );
	}
	
	/** 
	 * Name may not contain / 
	 *
	 * @exception AssetException if given an illegal name
	 */
	public void        setName ( String s_name ) throws IllegalArgumentException {
		if ( 
                (-1 < s_name.indexOf ( '/' ))
                || s_name.startsWith( ".." )
                ) {
			throw new IllegalArgumentException ( "Illegal asset name: " + s_name );
		}
		os_name = s_name;
	}
	
	public void        setCreatorId ( UUID u_creator ) {  ou_creator = u_creator; }
	public void        setLastUpdaterId ( UUID u_last_updater ) {  ou_last_updater = u_last_updater; }
	public void        setAclId ( UUID u_acl ) {  ou_acl = u_acl; }
	/** Create a new owner object with x_owner as the only non-admin member */
	public void        setOwnerId ( UUID u_owner ) {
		ou_owner = u_owner;
	}
	public void        setAssetType ( AssetType n_type ) {  on_type = n_type; }
	public void        setComment ( String s_comment ) { os_comment = s_comment; }
	public void        setLastUpdate ( String s_last_update ) { os_last_update = s_last_update; }
	public void        setData ( String s_data ) throws ParseException { 
		if ( s_data.length () > OI_DATA_LIMIT ) {
			throw new TooBigException ( "Data exceeds 1024 characters" );
		}
		os_data = s_data; 
	}
	
	public void        setHomeId ( UUID u_home ) { ou_home = u_home; }
	public void        setFromId ( UUID u_from ) { ou_from = u_from; }
	public void        setToId ( UUID u_to ) { ou_to = u_to; }
	
	public void        setStartDate ( Date t_start ) { ot_start = t_start; }
	public void        setEndDate ( Date t_end ) { ot_end = t_end; }
	public void        setCreateDate ( Date t_create_date ) {  ot_create_date = t_create_date; }
	public void        setLastUpdateDate ( Date t_update_date ) {  ot_update_date = t_update_date; }
	public void        setLastAccessDate ( Date t_access_date ) {  ot_access_date = t_access_date; }
	
	public void        setValue ( float f_value ) { of_value = f_value; }
	
	/**
	 * Return a simple copy of this object
	 */
    @Override
	public SimpleAsset clone () {
		return (SimpleAsset) super.clone ();
	}

    @Override
	public String toString () { 
		return "Asset " + this.getAssetType () + " " + 
			this.getName () + " (" + this.getObjectId () + ")"; 
	}
    

    public void sync ( Asset a_copy_source ) throws InvalidAssetTypeException {
        if ( this == a_copy_source ) {
            return;
        }
        if ( ! getAssetType ().equals ( a_copy_source.getAssetType () ) ) {
            throw new InvalidAssetTypeException ( "Mismatching asset-types at sync: " +
                                                  this.getAssetType () + " != " +
                                                  a_copy_source.getAssetType ()
                                                  );
        }
        
        SimpleAsset a_simple_source = (SimpleAsset) a_copy_source;
        
        setObjectId ( a_simple_source.getObjectId () );
        setTransactionCount ( a_simple_source.getTransactionCount () );
        ou_creator = a_simple_source.ou_creator;
        ou_last_updater = a_simple_source.ou_last_updater;
        ou_acl = a_simple_source.ou_acl;
        on_type = a_simple_source.on_type;
        os_comment = a_simple_source.os_comment;
        os_last_update = a_simple_source.os_last_update;
        os_name = a_simple_source.os_name;
        of_value = a_simple_source.of_value;
        ou_owner = a_simple_source.ou_owner;
        os_data = a_simple_source.os_data;
        
        ou_home = a_simple_source.ou_home;
        ou_from = a_simple_source.ou_from;
        ou_to   = a_simple_source.ou_to;
        
        ot_start = a_simple_source.ot_start;
        ot_end   = a_simple_source.ot_end;
        
        ot_create_date = a_simple_source.ot_create_date;
        ot_update_date = a_simple_source.ot_update_date;
        ot_access_date = a_simple_source.ot_access_date;
    }
    
    
    public void save ( AssetManager m_asset, String s_update_comment
                       ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
    {
        this.sync ( m_asset.saveAsset ( this, s_update_comment ) );
    }

    public void sync ( AssetRetriever m_retriever 
                       ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
    {
        this.sync( m_retriever.getAsset( this.getObjectId () ) );
    }
    
}

