/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.asset.client;



import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.Callable;
import littleware.asset.Asset;
import littleware.base.LittleReference;
import littleware.base.cache.CacheableObject;
import littleware.base.event.LittleListener;
import littleware.base.event.LittleTool;

/**
 * Specialization of LittleReference for Asset references.
 * Fires LittleEvents when the client becomes aware that an
 * asset has a new child or a child leaves - stuff like that.
 * Works in conjunction with the AssetLibrary.
 * Allocate non-empty instance via assetLibrary.syncAsset( ) ...
 */
public interface AssetRef extends CacheableObject, LittleReference<Asset>, LittleTool {
    /**
     * Properties that propertyChangeListenrs can listen for.
     */
    public enum Operation {
        /** The Asset has been updated to a new instance or changed in an arbitrary way */
        assetUpdated,
        /** There has been some change in the set of assets where x.getFromId()==this.getAsset().getObjectId() */
        assetsLinkingFrom,
        /** 
         * There has been some change in the set of assets where
         * x.getToId()==this.getId()
         * or x.getLink( name ) refers to this.getId
         */
        assetsLinkingTo,
        /** The asset has been deleted from the repository, this model removed from the AssetLibrary */
        assetDeleted
    }


    @Override
    public AssetRef updateRef( Asset value );

    /**
     * Alias for updateRef( asset ).get()
     */
    public Asset syncAsset ( Asset asset );
    
    
    public static AssetRef EMPTY = new AssetRef() {
        private Iterator<Asset>  emptyIterator = new Iterator<Asset>() {

            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public Asset next() {
                throw new NoSuchElementException();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            
        };
        
        @Override
        public Asset syncAsset(Asset asset) {
            throw new UnsupportedOperationException("Not supported on empty reference.");
        }

        @Override
        public boolean isSet() {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public Asset getOr(Asset t) {
            return t;
        }

        @Override
        public Asset getOrCall(Callable<Asset> clbl) throws Exception {
            return clbl.call();
        }

        @Override
        public Asset get() {
            throw new NoSuchElementException();
        }

        @Override
        public Asset getRef() {
            return get();
        }

        @Override
        public AssetRef updateRef(Asset t) {
            throw new UnsupportedOperationException("Not supported on empty reference.");
        }

        @Override
        public void clear() {
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener pl) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener pl) {
        }

        @Override
        public void addLittleListener(LittleListener ll) {
        }

        @Override
        public void removeLittleListener(LittleListener ll) {
        }

        @Override
        public UUID getId() {
            throw new UnsupportedOperationException("Not supported on empty reference.");
        }

        @Override
        public long getTimestamp() {
            return 0L;
        }

        @Override
        public Iterator<Asset> iterator() {
            return emptyIterator;
        }

    @Override
    public boolean nonEmpty() {
      return ! isEmpty();
    }

    @Override
    public Asset getOrThrow(RuntimeException re) {
      throw re;
    }

    @Override
    public Asset getOrThrow(Exception excptn) throws Exception {
      throw excptn;
    }

    };
}

