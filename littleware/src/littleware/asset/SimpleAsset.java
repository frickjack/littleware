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
import java.io.IOException;
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
public class SimpleAsset extends SimpleCacheableObject implements Asset, java.io.Serializable {

    private static final Factory<UUID> ofactory_uuid = UUIDFactory.getFactory();
    private static final Logger olog_generic = Logger.getLogger(SimpleAsset.class.getName());
    private UUID ou_creator = null;
    private UUID ou_last_updater = null;
    private UUID ou_acl = null;
    private AssetType on_type = AssetType.GENERIC;
    private String os_comment = "";
    private String os_last_update = "";
    private String os_name = "never_initialized";
    private float of_value = 0;
    private UUID ou_owner = null;
    private String os_data = "";
    private UUID ou_home = null;
    private UUID ou_from = null;
    private UUID ou_to = null;
    private Date ot_start = null;
    private Date ot_end = null;
    private Date ot_create_date = new Date();
    private Date ot_update_date = new Date();
    private Date ot_access_date = new Date();
    /** Supporting java serialization */
    private static final long serialVersionUID = 42L;
    /** Limit on the size of the data block */
    public static final int OI_DATA_LIMIT = 1024;
    private transient PropertyChangeSupport osupport_props = new PropertyChangeSupport(this);
    private boolean ob_dirty = true;
    private int oi_state = 0;

    /** 
     * Wrapper fires property change if property changes, and
     * updates inSync property if necessary.
     * 
     * @param sProp property that changes
     * @param oldValue
     * @param newValue
     */
    protected void firePropertyChange(String sProp, Object oldValue, Object newValue) {
        if (!Whatever.equalsSafe(oldValue, newValue)) {
            osupport_props.firePropertyChange(sProp, oldValue, newValue);
            setDirty(true);
        }
    }

    @Override
    public void addPropertyChangeListener(
            PropertyChangeListener listen_props) {
        osupport_props.addPropertyChangeListener(listen_props);
    }

    @Override
    public void removePropertyChangeListener(
            PropertyChangeListener listen_props) {
        osupport_props.removePropertyChangeListener(listen_props);
    }

    /**
     * Give subtypes direct access to the PropertyChangeSupport.
     * In general subtypes should prefer to call
     *     SimpleAsset.firePropertyChangeSupport
     * which also takes care of updating InSync
     */
    protected PropertyChangeSupport getPropertyChangeSupport() {
        return osupport_props;
    }

    @Override
    public boolean isDirty() {
        return ob_dirty;
    }

    @Override
    public void setDirty(boolean bDirty) {
        final boolean old = ob_dirty;
        ob_dirty = bDirty;
        osupport_props.firePropertyChange("dirty", old, ob_dirty);
    }

    /**
     * Constructor does some basic initialization.
     * Sets type to AssetType.GENERIC, object-id to new id.
     */
    public SimpleAsset() {
        try {
            this.setObjectId(ofactory_uuid.create());
        } catch (FactoryException e) {
            olog_generic.log(Level.FINE, "New SimpleAsset left with minimum initialization, caught: " + e);
        }
    }

    @Override
    public String getName() {
        return os_name;
    }

    @Override
    public UUID getCreatorId() {
        return ou_creator;
    }

    @Override
    public UUID getLastUpdaterId() {
        return ou_last_updater;
    }

    @Override
    public UUID getAclId() {
        return ou_acl;
    }

    @Override
    public AssetType getAssetType() {
        return on_type;
    }

    @Override
    public String getComment() {
        return os_comment;
    }

    @Override
    public String getLastUpdate() {
        return os_last_update;
    }

    @Override
    public String getData() {
        return os_data;
    }

    @Override
    public UUID getFromId() {
        return ou_from;
    }

    @Override
    public UUID getToId() {
        return ou_to;
    }

    @Override
    public UUID getHomeId() {
        return ou_home;
    }

    @Override
    public UUID getOwnerId() {
        return ou_owner;
    }

    @Override
    public Date getStartDate() {
        return ot_start;
    }

    @Override
    public Date getEndDate() {
        return ot_end;
    }

    @Override
    public Date getCreateDate() {
        return ot_create_date;
    }

    @Override
    public Date getLastUpdateDate() {
        return ot_update_date;
    }

    @Override
    public Date getLastAccessDate() {
        return ot_access_date;
    }

    @Override
    public Float getValue() {
        return of_value;
    }

    @Override
    public Asset getHome(AssetRetriever m_retriever) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        if (null == ou_home) {
            return null;
        }
        return m_retriever.getAsset(ou_home).get();
    }

    @Override
    public Owner getOwner(AssetRetriever m_retriever)
            throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        if (null == ou_owner) {
            return new SimpleOwner(
                    m_retriever.getAsset(AccountManager.UUID_ADMIN_GROUP).get().narrow(LittleGroup.class));
        }

        return new SimpleOwner(m_retriever.getAsset(ou_owner).get().narrow(LittleUser.class),
                m_retriever.getAsset(AccountManager.UUID_ADMIN_GROUP).get().narrow(LittleGroup.class));
    }

    @Override
    public LittleAcl getAcl(AssetRetriever m_retriever)
            throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        if (null == ou_acl) {
            return null;
        }
        return m_retriever.getAsset(ou_acl).get().narrow(LittleAcl.class);
    }

    @Override
    public LittleUser getCreator(AssetRetriever m_retriever)
            throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        if (null == ou_creator) {
            return null;
        }
        return m_retriever.getAsset(ou_creator).get().narrow(LittleUser.class);
    }

    @Override
    public LittleUser getLastUpdater(AssetRetriever m_retriever)
            throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        if (null == ou_last_updater) {
            return null;
        }
        return m_retriever.getAsset(ou_last_updater).get().narrow(LittleUser.class);
    }

    @Override
    public Asset getFromAsset(AssetRetriever m_retriever)
            throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        if (null == ou_from) {
            return null;
        }
        return m_retriever.getAsset(ou_from).get();
    }

    @Override
    public Asset getToAsset(AssetRetriever m_retriever)
            throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        if (null == ou_to) {
            return null;
        }
        return m_retriever.getAsset(ou_to).get();
    }

    /**
     * Name may not contain /
     *
     * @exception AssetException if given an illegal name
     */
    @Override
    public void setName(String s_name) throws IllegalArgumentException {
        if ((-1 < s_name.indexOf('/')) || s_name.startsWith("..")) {
            throw new IllegalArgumentException("Illegal asset name: " + s_name);
        }
        final String sOld = os_name;
        os_name = s_name;
        firePropertyChange(s_name, sOld, s_name);
    }

    @Override
    public void setCreatorId(UUID u_creator) {
        final UUID old = ou_creator;
        ou_creator = u_creator;
        firePropertyChange("creatorId", old, u_creator);
    }

    @Override
    public void setLastUpdaterId(UUID u_last_updater) {
        final UUID old = ou_last_updater;
        ou_last_updater = u_last_updater;
        firePropertyChange("lastUpdaterId", old, ou_last_updater);
    }

    @Override
    public void setAclId(UUID u_acl) {
        final UUID old = ou_acl;
        ou_acl = u_acl;
        firePropertyChange("aclId", old, ou_acl);
    }

    /** Create a new owner object with x_owner as the only non-admin member */
    @Override
    public void setOwnerId(UUID u_owner) {
        final UUID old = ou_owner;
        ou_owner = u_owner;
        firePropertyChange("owner", old, ou_owner);
    }

    @Override
    public void setAssetType(AssetType n_type) {
        final AssetType old = on_type;
        on_type = n_type;
        firePropertyChange("assetType", old, on_type);
    }

    @Override
    public void setComment(String s_comment) {
        final String old = os_comment;
        os_comment = s_comment;
        firePropertyChange("comment", old, os_comment);
    }

    @Override
    public void setLastUpdate(String s_last_update) {
        final String old = os_last_update;
        os_last_update = s_last_update;
        firePropertyChange("lastUpdate", old, s_last_update);
    }

    @Override
    public void setData(String s_data) throws ParseException {
        if (s_data.length() > OI_DATA_LIMIT) {
            throw new TooBigException("Data exceeds 1024 characters");
        }
        final String old = os_data;
        os_data = s_data;
        firePropertyChange("data", old, s_data);
    }

    @Override
    public void setHomeId(UUID u_home) {
        final UUID old = ou_home;
        ou_home = u_home;
        firePropertyChange("homeId", old, ou_home);
    }

    @Override
    public void setFromId(UUID u_from) {
        final UUID old = ou_from;
        ou_from = u_from;
        firePropertyChange("fromId", old, ou_from);
    }

    @Override
    public void setToId(UUID u_to) {
        final UUID old = ou_to;
        ou_to = u_to;
        firePropertyChange("toId", old, ou_to);
    }

    @Override
    public void setStartDate(Date t_start) {
        final Date old = ot_start;
        ot_start = t_start;
        firePropertyChange("startDate", old, ot_start);
    }

    @Override
    public void setEndDate(Date t_end) {
        final Date old = ot_end;
        ot_end = t_end;
        firePropertyChange("endDate", old, ot_end);
    }

    @Override
    public void setCreateDate(Date t_create_date) {
        final Date old = ot_create_date;
        ot_create_date = t_create_date;
        firePropertyChange("createDate", old, ot_create_date);
    }

    @Override
    public void setLastUpdateDate(Date t_update_date) {
        final Date old = ot_update_date;
        ot_update_date = t_update_date;
        firePropertyChange("lastUpdateDate", old, ot_update_date);
    }

    @Override
    public void setLastAccessDate(Date t_access_date) {
        ot_access_date = t_access_date;
    }

    @Override
    public void setValue(float f_value) {
        final float old = of_value;
        of_value = f_value;
        firePropertyChange( "value", old, f_value );
    }

    /**
     * Return a simple copy of this object
     */
    @Override
    public SimpleAsset clone() {
        return (SimpleAsset) super.clone();
    }

    @Override
    public void setTransactionCount( long lCount ) {
        final long old = getTransactionCount();
        super.setTransactionCount( lCount );
        firePropertyChange( "transactionCount", old, lCount );
    }

    @Override
    public String toString() {
        return "Asset " + this.getAssetType() + " " +
                this.getName() + " (" + this.getObjectId() + ")";
    }

    @Override
    public void sync(Asset a_copy_source) {
        if (this == a_copy_source) {
            return;
        }
        if (!getAssetType().equals(a_copy_source.getAssetType())) {
            throw new IllegalArgumentException("Mismatching asset-types at sync: " +
                    this.getAssetType() + " != " +
                    a_copy_source.getAssetType());
        }

        SimpleAsset a_simple_source = (SimpleAsset) a_copy_source;

        setObjectId(a_simple_source.getObjectId());
        setTransactionCount(a_simple_source.getTransactionCount());
        setCreatorId( a_simple_source.ou_creator );
        setLastUpdaterId( a_simple_source.ou_last_updater );
        setAclId( a_simple_source.ou_acl );
        setComment( a_simple_source.os_comment );
        setLastUpdate( a_simple_source.os_last_update );
        setName( a_simple_source.os_name );
        setValue( a_simple_source.of_value );
        setState( a_simple_source.oi_state );
        setOwnerId( a_simple_source.ou_owner );
        try {
            // simplify XML-data handling
            setData(a_simple_source.getData());
        } catch (ParseException ex) {
            throw new AssertionFailedException("Unexpected exception", ex);
        }

        setHomeId( a_simple_source.ou_home );
        setFromId( a_simple_source.ou_from );
        setToId( a_simple_source.ou_to );

        setStartDate( a_simple_source.ot_start );
        setEndDate( a_simple_source.ot_end );

        setCreateDate( a_simple_source.ot_create_date );
        setLastUpdateDate( a_simple_source.ot_update_date );
        setLastAccessDate( a_simple_source.ot_access_date );
        setDirty(a_simple_source.isDirty());
    }

    @Override
    public void save(AssetManager m_asset, String s_update_comment) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        this.sync(m_asset.saveAsset(this, s_update_comment));
    }

    @Override
    public void sync(AssetRetriever m_retriever) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        this.sync(m_retriever.getAsset(this.getObjectId()).get());
    }

    @Override
    public <T extends Asset> T narrow(Class<T> type) {
        return type.cast(this);
    }

    @Override
    public <T extends Asset> T narrow() {
        return (T) this;
    }

    @Override
    public Integer getState() {
        return oi_state;
    }

    @Override
    public void setState(int iState) {
        final int old = oi_state;
        oi_state = iState;
        firePropertyChange( "state", old, iState );
    }

    private void readObject(java.io.ObjectInputStream in)
     throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        osupport_props = new PropertyChangeSupport(this);
    }  
}

