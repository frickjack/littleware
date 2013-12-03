/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.client.internal;

import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import java.util.logging.Level;
import littleware.asset.client.AssetRef;
import littleware.asset.client.AssetLibrary;

import littleware.asset.client.AssetActionEvent;
import littleware.asset.Asset;
import littleware.asset.AssetType;
import littleware.asset.InvalidAssetTypeException;
import littleware.asset.client.spi.LittleServiceBus;
import littleware.asset.spi.AbstractAsset;
import littleware.base.Options;
import littleware.base.Option;
import littleware.base.event.LittleEvent;
import littleware.base.event.LittleListener;
import littleware.base.event.helper.SimpleLittleTool;
import littleware.base.validate.ValidationException;

/**
 * Simple implementation of AssetLibrary interface for
 * in-memory asset model cache.
 * Intended to be a singleton PER-USER.
 */
public class SimpleAssetLibrary
        implements AssetLibrary {

    private static final Logger log = Logger.getLogger(SimpleAssetLibrary.class.getName());
    private final com.google.common.cache.Cache<UUID, SimpleAssetRef> cache = CacheBuilder.newBuilder().softValues().build();
    private final com.google.common.cache.Cache<String, UUID> nameMap = CacheBuilder.newBuilder().softValues().build();

    @Inject
    public SimpleAssetLibrary( LittleServiceBus eventBus ) {
        eventBus.addLittleListener( new AssetLibServiceListener( eventBus, this ) );
    }
    
    @Override
    public AssetRef getByName(String name, AssetType atype) throws InvalidAssetTypeException {
        if (!atype.isNameUnique()) {
            throw new InvalidAssetTypeException("Asset type not name-unique: " + atype);
        }
        final UUID id = nameMap.getIfPresent(atype.toString() + "/" + name);
        if (null == id) {
            return AssetRef.EMPTY;
        }
        return getById(id);
    }

    @Override
    public AssetRef syncAsset(Asset asset) {
        if (null == asset) {
            return AssetRef.EMPTY;
        }
        SimpleAssetRef ref = cache.getIfPresent(asset.getId());

        if (null == ref) {
            ref = new SimpleAssetRef(asset, this);
            cache.put(asset.getId(), ref);
            if ( asset.getAssetType().isNameUnique() ) {
                this.nameMap.put( nameKey( asset ), asset.getId() );
            }
            final AssetActionEvent event = new AssetActionEvent( ref, AssetRef.Operation.assetUpdated );
            notifyNeighbors( (AbstractAsset) asset,event, new HashSet<UUID>() );
        } else {
            ref.syncAsset(asset);
        }

        return ref;
    }

    @Override
    public void assetDeleted(UUID deleteId) {
        final SimpleAssetRef deletedRef = cache.getIfPresent(deleteId);
        cache.invalidate(deleteId);
        if (null != deletedRef) {
            final Asset asset = deletedRef.get();

            if (asset.getAssetType().isNameUnique()) {
                for (AssetType atype = asset.getAssetType();
                        (atype != null) && atype.isNameUnique();
                        atype = (AssetType) atype.getSuperType().getOr(null)) {
                    nameMap.invalidate(atype.toString() + "/" + asset.getName());
                }
            }

            final AssetActionEvent event = new AssetActionEvent(deletedRef,
                    AssetRef.Operation.assetDeleted);
            deletedRef.fireLittleEvent(event);
            notifyNeighbors( (AbstractAsset) asset, event, new HashSet<UUID>() );
        }
    }

    /**
     * Little utility fires AssetActionEvents on the references
     * linking to/from the given ref that has been updated in some way
     *
     * @param parentEvent
     * @param alreadyNotified set of asset-ids already notified of event - modified in place
     * @return alreadyNotified with new ids added
     */
    private Set<UUID> notifyNeighbors( AbstractAsset asset, AssetActionEvent parentEvent, Set<UUID> alreadyNotified ) {
        alreadyNotified.add( asset.getId() );
        if ( (null != asset.getFromId()) && (! alreadyNotified.contains( asset.getFromId() ))) {
            alreadyNotified.add(asset.getFromId());
            final SimpleAssetRef parent = cache.getIfPresent(asset.getFromId());
            if (null != parent) {
                parent.fireLittleEvent(
                        new AssetActionEvent(parent,
                        AssetRef.Operation.assetsLinkingFrom,
                        parentEvent));
            }
        }
        final Set<UUID> neighbors = new HashSet<>();
        if( null != asset.getToId() ) {
            neighbors.add( asset.getToId() );
        }
        for( UUID neighborId : asset.getLinkMap().values() ) {
            neighbors.add( neighborId );
        }
        for( UUID neighborId : neighbors ) {
            if ( (null != neighborId) && (! alreadyNotified.contains( neighborId ) ) ) {
                alreadyNotified.add( neighborId );
            }
            final SimpleAssetRef neighbor = cache.getIfPresent(neighborId);
            if (null != neighbor) {
                neighbor.fireLittleEvent(
                        new AssetActionEvent(neighbor,
                        AssetRef.Operation.assetsLinkingTo,
                        parentEvent));
            }
        }
        return alreadyNotified;
    }

    @Override
    public AssetRef getById(UUID id) {
        if (null == id) {
            return AssetRef.EMPTY;
        }
        final AssetRef ref = cache.getIfPresent(id);
        if (null == ref) {
            return AssetRef.EMPTY;
        }
        return ref;
    }

    private String nameKey( Asset asset ) {
        return asset.getAssetType().toString() + "/" + asset.getName();
    }

    //----------------------------------------
    /**
     * Simple implementation of AssetRef interface
     */
    private static class SimpleAssetRef implements AssetRef {

        private Option<Asset> asset = Options.empty();
        private final SimpleLittleTool eventSupport = new SimpleLittleTool(this);
        private final SimpleAssetLibrary library;

        /**
         * Constructor associates an asset
         */
        public SimpleAssetRef(Asset asset, SimpleAssetLibrary library) {
            this.asset = Options.some( asset );
            this.library = library;
            ValidationException.validate( asset != null, "Must reference non-null asset");
        }

        @Override
        public Asset get() {
            return asset.get();
        }


        /**
         * Call out to SimpleAssetLibrary.sycnAsset to
         * fire AssetActionEvent on other affected asset-models.
         */
        @Override
        public Asset syncAsset(Asset newAsset) {
            log.log(Level.FINE, "Syncing: {0}", newAsset);

            if ((null == newAsset) || (asset.getOr(null) == newAsset)) {
                return newAsset;
            }
            if ( asset.isEmpty() ) {
                asset = Options.some(newAsset);
            }
            if ( newAsset.getTimestamp() <= asset.get().getTimestamp() ) {
                return asset.get();
            }
            // this call syncs a_new with oa_data
            final Asset oldAsset = asset.get();
            asset = Options.some(newAsset );

            if ( ! oldAsset.getName().equals( newAsset.getName() )) {
                library.nameMap.invalidate( library.nameKey( oldAsset ) );
                if ( newAsset.getAssetType().isNameUnique() ) {
                    library.nameMap.put( library.nameKey( newAsset ), newAsset.getId() );
                }
            }
            final AssetActionEvent event = new AssetActionEvent(
                    this, AssetRef.Operation.assetUpdated
                    );
            eventSupport.fireLittleEvent( event );
            library.notifyNeighbors( (AbstractAsset) newAsset, event, new HashSet<UUID>() );
            return asset.get();
        }


        /**
         * Just return the hashCode for the wrapped asset
         */
        @Override
        public int hashCode() {
            return asset.hashCode();
        }

        @Override
        public String toString() {
            return "AssetRef(" + asset.toString() + ")";
        }

        /** Just compare the wrapped assets */
        @Override
        public boolean equals(Object other) {
            return ((null != other) && (other instanceof SimpleAssetRef) && asset.equals(((SimpleAssetRef) other).asset));
        }

        /**
         * Internal convenience - calls through to internal SimpleLittleTool
         */
        void fireLittleEvent(LittleEvent event_fire) {
            eventSupport.fireLittleEvent(event_fire);
        }

        @Override
        public void addLittleListener(LittleListener listen_add) {
            eventSupport.addLittleListener(listen_add);
        }

        @Override
        public void removeLittleListener(LittleListener listen_remove) {
            eventSupport.removeLittleListener(listen_remove);
        }

        @Override
        public AssetRef updateRef(Asset value) {
            syncAsset( value );
            return this;
        }

        @Override
        public void clear() {
            asset = Options.empty();
            final AssetActionEvent event = new AssetActionEvent(
                    this, AssetRef.Operation.assetDeleted
                    );
            eventSupport.fireLittleEvent( event );
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener pl) {
            eventSupport.addPropertyChangeListener(pl);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener pl) {
            eventSupport.removePropertyChangeListener(pl);
        }

        @Override
        public UUID getId() {
            return asset.get().getId();
        }

        @Override
        public long getTimestamp() {
            return asset.get().getTimestamp();
        }


        @Override
        public boolean isSet() {
            return true;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public Asset getOr(Asset t) {
            return asset.getOr( t );
        }

        @Override
        public Asset getOrCall(Callable<Asset> clbl) throws Exception {
            return asset.getOrCall( clbl );
        }


        @Override
        public Asset getRef() {
            return asset.get();
        }

        @Override
        public Iterator<Asset> iterator() {
            return asset.iterator();
        }

    @Override
    public boolean nonEmpty() {
      return ! isEmpty();
    }

    @Override
    public Asset getOrThrow(RuntimeException re) {
      return asset.getOrThrow(re);
    }

    @Override
    public Asset getOrThrow(Exception excptn) throws Exception {
      return asset.getOrThrow( excptn );
    }

    }
}
