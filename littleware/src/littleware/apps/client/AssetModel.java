/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.client;


import littleware.asset.Asset;

/**
 * Little Observable hook to fascilitate MVC design
 * with in-memory asset-data manipulated by UI
 * controls or whatever.
 * An AssetView may listen as a PropertyChangeListener
 * on an AssetModel for fired PropertyChangeEvent.
 * The AssetModelLibrary may periodically replace a model&apos;s Asset
 * with a new up-to-date instance of the Asset -
 * the AssetModel notifies its listeners by firing a PropertyChangeEvent.
 * Works in conjunction with the AssetModelLibrary.
 */
public interface AssetModel extends Comparable<AssetModel> {
    /**
     * Properties that propertyChangeListenrs can listen for.
     */
    public enum Operation {
        /** The Asset has been updated to a new instance or changed in an arbitrary way */
        assetUpdated,
        /** There has been some change in the set of assets where x.getFromId()==this.getAsset().getObjectId() */
        assetsLinkingFrom,
        /** The asset has been deleted from the repository, this model removed from the AssetModelLibrary */
        assetDeleted
    }
    
    /**
     * Get the Asset associated with this model
     */
    public Asset getAsset ();
        
    /* @deprecated ....
     * Reset the asset this model references - triggers
     * an AssetModelEvent to listeners.
     *
     * @param a_data for getAsset to return from now on
     * @exception IllegalArgumentException RuntimeException if a_data.getObjectId is
     *              not the same as the id of the asset this Model is
     *              already observing
     *
    public void setAsset ( Asset a_data );
     */
    
    /**
     * Little utility function - getAsset.sync if
     *     a_data.getTransactionCount () > getAsset().getTransactionCount ()
     * or a_data.isDirty() and fires various AssetModelEvents
     * depending on state change.
     *
     * @param a_data for getAsset to return from now on if a_data has newer TransactionCount
     * @return (a_data.getTransactionCount () > getAsset ().getTransactionCount ()) ? a_data : getAsset ()
     * @exception IllegalArgumentException RuntimeExcption if object-ids do not match
     */
    public Asset syncAsset ( Asset a_data );

    /**
     * Get the library with which this model is associated with
     */
    public AssetModelLibrary getLibrary ();
    
    /**
     * Allow observers to listen for property changes 
     *
     * @param listen_props listener that wants informed when a setter gets invoked on this object
     */
    public void addLittleListener( LittleListener listen_props );
    
    /**
     * Allow observers to stop listening for changes
     *
     * @param listen_props to stop sending events to
     */
    public void removeLittleListener( LittleListener listen_props );
}

