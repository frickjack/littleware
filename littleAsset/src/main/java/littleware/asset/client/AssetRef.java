package littleware.asset.client;



import com.google.common.base.Supplier;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
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


    
    public static final AssetRef EMPTY = new AssetRef() {
        @Override
        public UUID getId() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public long getTimestamp() {
            return 0L;
        }

        @Override
        public Optional<Asset> asOptional() {
            return Optional.empty();
        }

        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        
        @Override
        public Asset orElse(Asset alt) {
            return alt;
        }

        @Override
        public Asset orElseGet(Supplier<? extends Asset> supplier) {
            return supplier.get();
        }

        @Override
        public void ifPresent(Consumer<? super Asset> consumer) {}

        @Override
        public Asset get() {
            throw new NoSuchElementException();
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listen_props) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listen_props) {
        }

                @Override
        public Iterator<Asset> iterator() {
            return new Iterator<Asset>() {
                    @Override
                    public boolean hasNext() {
                        return false;
                    }

                    @Override
                    public Asset next() {
                        throw new NoSuchElementException();
                    }
                };
        }

        @Override
        public void addLittleListener(LittleListener listener) {
        }

        @Override
        public void removeLittleListener(LittleListener listener) {
        }
    };
}

