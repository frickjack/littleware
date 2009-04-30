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
	private static final     Logger         olog_generic = Logger.getLogger ( SimpleAsset.class.getName() );
	
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
    
    @Override
    public void addPropertyChangeListener(
                                          PropertyChangeListener listen_props )
    {
        osupport_props.addPropertyChangeListener( listen_props );
    }
    @Override
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
	
    @Override
	public String      getName () { return os_name; }
    @Override
	public UUID        getCreatorId () { return ou_creator; }
    @Override
	public UUID        getLastUpdaterId () { return ou_last_updater; }
    @Override
	public UUID        getAclId () { return ou_acl; }
    @Override
	public AssetType   getAssetType () { return on_type; }
    @Override
	public String      getComment () { return os_comment; }
    @Override
	public String      getLastUpdate () { return os_last_update; }
    @Override
	public String      getData () { return os_data; }
	
    @Override
	public UUID        getFromId () { return ou_from; }
    @Override
	public UUID        getToId () { return ou_to; }
    @Override
	public UUID        getHomeId () { return ou_home; }
    @Override
	public UUID        getOwnerId () { return ou_owner; }
	
    @Override
	public Date        getStartDate () { return ot_start; }
    @Override
	public Date        getEndDate () { return ot_end; }
    @Override
	public Date        getCreateDate () { return ot_create_date; }
    @Override
	public Date        getLastUpdateDate () { return ot_update_date; }
    @Override
	public Date        getLastAccessDate () { return ot_access_date; }
	
    @Override
	public Float       getValue () { return of_value; }
	

    @Override
	public Asset getHome ( AssetRetriever m_retriever ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException {
		if ( null == ou_home ) {
			return null;
		}
		return m_retriever.getAsset ( ou_home );
	}

    @Override
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

    @Override
	public LittleAcl getAcl ( AssetRetriever m_retriever ) 
		throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		if ( null == ou_acl ) {
			return null;
		}
		return (LittleAcl) m_retriever.getAsset ( ou_acl );
	}

    @Override
	public LittleUser getCreator ( AssetRetriever m_retriever ) 
		throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		if ( null == ou_creator ) {
			return null;
		}
		return (LittleUser) m_retriever.getAsset ( ou_creator );
	}

    @Override
	public LittleUser getLastUpdater ( AssetRetriever m_retriever ) 
	    throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		if ( null == ou_last_updater ) {
			return null;
		}
		return (LittleUser) m_retriever.getAsset ( ou_last_updater );
	}

    @Override
	public Asset getFromAsset ( AssetRetriever m_retriever ) 
		throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		if ( null == ou_from ) {
			return null;
		}
		return m_retriever.getAsset ( ou_from );
	}

    @Override
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
    @Override
	public void        setName ( String s_name ) throws IllegalArgumentException {
		if ( 
                (-1 < s_name.indexOf ( '/' ))
                || s_name.startsWith( ".." )
                ) {
			throw new IllegalArgumentException ( "Illegal asset name: " + s_name );
		}
		os_name = s_name;
	}
	
    @Override
	public void        setCreatorId ( UUID u_creator ) {  ou_creator = u_creator; }
    @Override
	public void        setLastUpdaterId ( UUID u_last_updater ) {  ou_last_updater = u_last_updater; }
    @Override
	public void        setAclId ( UUID u_acl ) {  ou_acl = u_acl; }
	/** Create a new owner object with x_owner as the only non-admin member */
    @Override
	public void        setOwnerId ( UUID u_owner ) {
		ou_owner = u_owner;
	}
    @Override
	public void        setAssetType ( AssetType n_type ) {  on_type = n_type; }
    @Override
	public void        setComment ( String s_comment ) { os_comment = s_comment; }
    @Override
	public void        setLastUpdate ( String s_last_update ) { os_last_update = s_last_update; }
    @Override
	public void        setData ( String s_data ) throws ParseException { 
		if ( s_data.length () > OI_DATA_LIMIT ) {
			throw new TooBigException ( "Data exceeds 1024 characters" );
		}
		os_data = s_data; 
	}
	
    @Override
	public void        setHomeId ( UUID u_home ) { ou_home = u_home; }
    @Override
	public void        setFromId ( UUID u_from ) { ou_from = u_from; }
    @Override
	public void        setToId ( UUID u_to ) { ou_to = u_to; }
	
    @Override
	public void        setStartDate ( Date t_start ) { ot_start = t_start; }
    @Override
	public void        setEndDate ( Date t_end ) { ot_end = t_end; }
    @Override
	public void        setCreateDate ( Date t_create_date ) {  ot_create_date = t_create_date; }
    @Override
	public void        setLastUpdateDate ( Date t_update_date ) {  ot_update_date = t_update_date; }
    @Override
	public void        setLastAccessDate ( Date t_access_date ) {  ot_access_date = t_access_date; }
	
    @Override
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
    

    @Override
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
        try {
            // simplify XML-data handling
            setData(a_simple_source.getData());
        } catch (ParseException ex) {
            throw new AssertionFailedException( "Unexpected exception", ex );
        }
        
        ou_home = a_simple_source.ou_home;
        ou_from = a_simple_source.ou_from;
        ou_to   = a_simple_source.ou_to;
        
        ot_start = a_simple_source.ot_start;
        ot_end   = a_simple_source.ot_end;
        
        ot_create_date = a_simple_source.ot_create_date;
        ot_update_date = a_simple_source.ot_update_date;
        ot_access_date = a_simple_source.ot_access_date;
    }
    
    
    @Override
    public void save ( AssetManager m_asset, String s_update_comment
                       ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
    {
        this.sync ( m_asset.saveAsset ( this, s_update_comment ) );
    }

    @Override
    public void sync ( AssetRetriever m_retriever 
                       ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
    {
        this.sync( m_retriever.getAsset( this.getObjectId () ) );
    }
    
}

