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

import com.google.inject.Singleton;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.apps.client.event.AssetModelEvent;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.asset.AssetRetriever;
import littleware.asset.AssetSearchManager;
import littleware.asset.AssetType;
import littleware.asset.InvalidAssetTypeException;
import littleware.base.AssertionFailedException;
import littleware.base.BaseException;
import littleware.base.SimpleCache;
import littleware.base.Whatever;

/**
 * Simple implementation of AssetModelLibrary interface for
 * in-memory asset model cache.
 * Intended to be a singleton PER-USER.
 */
@Singleton
public class SimpleAssetModelLibrary extends SimpleCache<UUID,AssetModel> 
        implements AssetModelLibrary 
{
    private static final Logger   olog_generic = Logger.getLogger ( SimpleAssetModelLibrary.class.getName () );

    @Override
    public Asset retrieveAsset(UUID u_id, AssetRetriever retriever) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        AssetModel model = retrieveAssetModel( u_id, retriever );
        if ( null == model ) {
            return null;
        }
        return model.getAsset();
    }
    
    /**
     * Simple implementation of AssetModel interface
     */
    private class SimpleAssetModel implements AssetModel {
        private       Asset              oa_data = null;
        private       SimpleLittleTool   otool_support = new SimpleLittleTool ( this );
        
        /** Do-nothing constructor - must call setAssetModel later */
        public SimpleAssetModel () {}
        
        /**
         * Constructor associates an asset 
         */
        public SimpleAssetModel ( Asset a_data ) {
            oa_data = a_data;
        }
        
        @Override
        public Asset getAsset () {
            return oa_data;
        }
        
        /**
         * Internal version of setAsset that returns the AssetModelEvent
         * that gets fired (if any) so that it may be used as a cause.
         */
        private AssetModelEvent  setAssetReturnEvent ( Asset a_data ) {
            AssetModelEvent event_result = null;

            if ( (a_data != null)
                 && (a_data != oa_data)
                 ) {
                Asset a_old = oa_data;
                oa_data = a_data;
                
                // update by-name dictionary
                if ( a_data.getAssetType().isNameUnique()
                     && (
                     (null == a_old)
                     || (! a_old.getName().equals( a_data.getName() ))
                     )
                        ) {
                    if ( null != a_old ) {
                        for( AssetType atype = a_old.getAssetType();
                             (atype != null) && atype.isNameUnique();
                             atype = atype.getSuperType()
                            ) {
                            omulti_byname.get( atype ).remove( a_old.getObjectId () );
                        }                        
                    }
                    for( AssetType atype = a_data.getAssetType();
                        (null != atype) && atype.isNameUnique();
                        atype = atype.getSuperType()
                    ) {
                        Map<String,UUID> map_4type = omulti_byname.get( atype );
                        if ( null == map_4type ) {
                            map_4type = new HashMap<String,UUID>();
                            omulti_byname.put( atype, map_4type );
                        }
                        map_4type.put( a_data.getName (), a_data.getObjectId () );
                    }
                }
                event_result = new AssetModelEvent ( this, AssetModel.Operation.assetUpdated );
                this.fireLittleEvent ( event_result );
            }
            return event_result;
        }
        
        @Override
        public void setAsset ( Asset a_data ) {
            setAssetReturnEvent ( a_data );
        }
                    
            
        /**
         * Call out to SimpleAssetModelLibrary.sycnAsset to
         * fire AssetModelEvent on other affected asset-models.
         */
        @Override
        public Asset syncAsset ( Asset a_new ) {
            Asset a_old = this.getAsset ();
            
            olog_generic.log ( Level.FINE, "Syncing: " + a_new );
            
            if ( (null == a_new) || (a_old == a_new) ) {
                return a_old;
            }
            if ( (null != a_old)
                 && (a_new.getTransactionCount () < a_old.getTransactionCount ())
                 ) {
                return a_old;
            } 
            AssetModelEvent event_cause = setAssetReturnEvent ( a_new );
            
            /**
             * Fire child AssetModelEvents if necessary
             */
            fireChildEvent ( a_old, a_new, event_cause );
            return this.getAsset ();            
        }
        
        
        @Override
        public AssetModelLibrary getLibrary () {
            return SimpleAssetModelLibrary.this;
        }
        
        /**
         * Just return the hashCode for the wrapped asset
         */
        @Override
        public int hashCode () {
            return oa_data.hashCode ();
        }
        
        @Override
        public String toString () {
            return "AssetModel(" + oa_data.toString () + ")";
        }
        
        /**
         * Just compare the wrapped assets
         */
        @Override
        public int compareTo ( AssetModel model_other ) {
            return oa_data.compareTo ( model_other.getAsset () );
        }
        
        /** Just compare the wrapped assets */
        @Override
        public boolean equals ( Object x_other ) {
            return ((null != x_other)
                    && (x_other instanceof SimpleAssetModel)
                    && oa_data.equals ( ((SimpleAssetModel) x_other).oa_data )
                    );
        }

        /**
         * Internal convenience - calls through to internal SimpleLittleTool
         */
        void fireLittleEvent ( LittleEvent event_fire ) {
            otool_support.fireLittleEvent ( event_fire );
        }
        
        @Override
        public void addLittleListener ( LittleListener listen_add ) {
            otool_support.addLittleListener ( listen_add );
        }
        
        @Override
        public void removeLittleListener ( LittleListener listen_remove ) {
            otool_support.removeLittleListener ( listen_remove );
        }
    }
    
    /**
     * Internal utility that fires an 
     * AssetModelEvent on the u_affected asset model if u_affected is not
     * null and its asset is in the library 
     *
     * @param u_affected to lookup
     * @param AssetModel.Operation n_operation for the child event
     * @param event_cause that is causing this child event - may be null
     * @return the event fired or null if none fired
     */
    private AssetModelEvent fireChildEvent ( UUID u_affected, 
                                             AssetModel.Operation n_operation,
                                             AssetModelEvent event_cause ) {
        if ( null == u_affected ) {
            return null;
        }
        SimpleAssetModel  amodel_affected = (SimpleAssetModel) get( u_affected );
        if ( null == amodel_affected ) {
            return null;
        }
        if ( amodel_affected == event_cause.getSource () ) {
            // Avoid simple loops
            return null;
        }
        AssetModelEvent    event_child = new AssetModelEvent ( amodel_affected,
                                                               n_operation,
                                                               null,
                                                               event_cause
                                                               );
        
        amodel_affected.fireLittleEvent ( event_child );
        return event_child;
    }
    
    /**
     * Internal utility compares a before and after asset,
     * and deteremined what child events need to be fired
     * based on the differences between the two.
     * For example, if a_new and a_from have different FromId,
     * then Operation.assetsLinkingFrom AssetModelEvents will
     * get fired on the referenced assets in the library to let them know that
     * their child layout has changed.
     * A null a_new is taken to indicate that the asset has been deleted
     * from the littleware repository.
     * A null a_old does not indicate asset creation - just indicates
     * that the asset was not yet loaded in the library, and results in a NOOP.
     *
     * @param a_old asset before change - null value causes NOOP
     * @param a_new asset after change - may be null to indicate
     *                         that the asset has been deleted
     * @param event_cause that caused the change between a_old and a_new
     */
    private void fireChildEvent ( Asset a_old, Asset a_new,
                                             AssetModelEvent event_cause ) {
        if ( (null == a_old) && (null == a_new) ) {
            return;
        }
        if ( (a_new != null) 
             && (null != a_old)
             && (! a_new.getObjectId ().equals ( a_old.getObjectId () ) ) 
             ) {
            throw new AssertionFailedException ( "Comparing assets with different ids" );
        } else if ( null == a_new ) {
            // Create a place-holder asset with null references to simplify things
            a_new = AssetType.GENERIC.create ();
            a_new.setFromId ( null );
            a_new.setToId ( null );
            a_new.setObjectId ( a_old.getObjectId () );
        } else if ( null == a_old ) {
            // Create a place-holder asset with null references to simplify things
            a_old = AssetType.GENERIC.create ();
            a_old.setFromId( null );
            a_old.setToId( null );
            a_old.setObjectId( a_new.getObjectId () );
        }
                 
        if ( ! Whatever.equalsSafe ( a_old.getFromId (), a_new.getFromId () ) ) {
             fireChildEvent ( a_old.getFromId (), AssetModel.Operation.assetsLinkingFrom, event_cause );
             fireChildEvent ( a_new.getFromId (), AssetModel.Operation.assetsLinkingFrom, event_cause );                
         }
             
    }
    
    private Map<AssetType,Map<String,UUID>> omulti_byname =
            new TreeMap<AssetType,Map<String,UUID>>();
    
    @Override
    public synchronized AssetModel getByName( String s_name, AssetType atype
            ) throws InvalidAssetTypeException
    {
        if ( ! atype.isNameUnique () ) {
            throw new InvalidAssetTypeException ( "Asset type not name-unique: " + atype );
        }
        Map<String,UUID> map_byname = omulti_byname.get( atype );
        if ( null == map_byname ) {
            return null;
        }
        UUID u_id = map_byname.get( s_name );
        if ( null == u_id ) {
            return null;
        }
        return get( u_id );
    }
    
    @Override
    public AssetModel getByName( String s_name, AssetType<? extends Asset> atype,
            AssetSearchManager m_search
            ) throws InvalidAssetTypeException,
        BaseException, 
        AssetException, GeneralSecurityException, RemoteException
    {
        AssetModel amodel_result = getByName( s_name, atype );
        
        if ( null != amodel_result ) {
            return amodel_result;
        }
        Asset a_result = m_search.getByName( s_name, atype ).getOr( null );
        if ( null == a_result ) {
            return null;
        }
        return syncAsset( a_result );
    }

    
    /**
     * Constructor initializes the underlying SimpleCache with huge
     * timeout and max-size
     */
    public SimpleAssetModelLibrary () {
        super( 10000000, 1000000 );
    }
        
    
    
    @Override
    public synchronized AssetModel syncAsset ( Asset a_new ) {
        if ( null == a_new ) {
            return null;
        }
        AssetModel amodel_lookup = this.get ( a_new.getObjectId () );
        
        if ( null == amodel_lookup ) { 
            amodel_lookup = new SimpleAssetModel ();
            this.put ( a_new.getObjectId (), amodel_lookup );
        }
        amodel_lookup.syncAsset ( a_new );            
        
        return amodel_lookup;
    }
    
    
    @Override
    public synchronized AssetModel retrieveAssetModel ( UUID u_id, AssetRetriever m_retriever ) throws BaseException, 
        AssetException, GeneralSecurityException, RemoteException
    {
        AssetModel amodel_lookup = this.get ( u_id );
        
        if ( null != amodel_lookup ) {
            return amodel_lookup;
        }
        Asset a_new = m_retriever.getAsset ( u_id ).getOr( null );
        if ( null == a_new ) {
            return null;
        }
        return syncAsset ( a_new );
    }

    @Override
    public Collection<AssetModel> syncAsset ( Collection<? extends Asset> v_assets ) {
        List<AssetModel> v_result = new ArrayList<AssetModel> ();
        for ( Asset a_check : v_assets ) {
            v_result.add( syncAsset ( a_check ) );
        }
        return v_result;
    }
    
    @Override
    public AssetModel remove( UUID u_remove ) {
        AssetModel amodel_remove = super.remove( u_remove );        
        if ( 
             (null != amodel_remove)
             && amodel_remove.getAsset().getAssetType().isNameUnique()             
            ) {
            Asset a_remove = amodel_remove.getAsset ();
            for ( AssetType atype = a_remove.getAssetType ();
                  (atype != null) && atype.isNameUnique();
                  atype = atype.getSuperType()
                ){
                Map<String,UUID> map_byname = omulti_byname.get( atype );
                if ( null != map_byname ) {
                    map_byname.remove( a_remove.getName () );
                }
            }
        }
        return amodel_remove;
    }
    
    
    @Override
    public AssetModel assetDeleted ( UUID u_deleted ) {
        final SimpleAssetModel amodel_deleted = (SimpleAssetModel) remove ( u_deleted );
        
        if ( null != amodel_deleted ) {
            AssetModelEvent  event_delete = new AssetModelEvent ( amodel_deleted,
                                                                  AssetModel.Operation.assetDeleted
                                                                  );
            amodel_deleted.fireLittleEvent ( event_delete );
            fireChildEvent ( amodel_deleted.getAsset (), null, event_delete );
        }
        return amodel_deleted;
    }
}

