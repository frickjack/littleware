package littleware.asset;

import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import littleware.asset.Asset;
import littleware.asset.client.AssetRef;
import littleware.base.event.LittleListener;

/**
 * Mock reference for testing.
 */
public class MockAssetRef implements AssetRef {
    private final Optional<Asset> ref;

    public MockAssetRef( Optional<Asset> ref ) {
        this.ref = ref;
    }

    
    @Override
    public UUID getId() {
        return ref.get().getId();
    }

    @Override
    public long getTimestamp() {
        return ref.get().getTimestamp();
    }

    @Override
    public boolean isPresent() {
        return ref.isPresent();
    }

    @Override
    public boolean isEmpty() {
        return ! ref.isPresent();
    }

    @Override
    public Asset orElse(Asset t) {
        return ref.orElse(t);
    }

    

    @Override
    public Asset get() {
        return ref.get();
    }

    


    @Override
    public void addPropertyChangeListener(PropertyChangeListener pl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addLittleListener(LittleListener ll) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeLittleListener(LittleListener ll) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Optional<Asset> asOptional() {
        return ref;
    }

    @Override
    public Asset orElseGet(Supplier<? extends Asset> supplier) {
        return ref.orElseGet(supplier);
    }

    @Override
    public void ifPresent(Consumer<? super Asset> consumer) {
        ref.ifPresent(consumer);
    }

    @Override
    public Iterator<Asset> iterator() {
        return new Iterator<Asset>(){
            private boolean isFirstTime = true;
            @Override
            public boolean hasNext() {
                return isFirstTime && ref.isPresent();
            }

            @Override
            public Asset next() {
                if ( hasNext() ) {
                    return ref.get();
                }
                throw new NoSuchElementException();
            }
        };
    }



}
