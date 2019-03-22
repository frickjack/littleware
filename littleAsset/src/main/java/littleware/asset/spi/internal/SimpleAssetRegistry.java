package littleware.asset.spi.internal;

import com.google.inject.Provider;
import java.util.NoSuchElementException;
import littleware.asset.AssetBuilder;
import littleware.asset.AssetType;
import littleware.asset.spi.AssetProviderRegistry;
import littleware.base.SimpleLittleRegistry;

public class SimpleAssetRegistry extends SimpleLittleRegistry<AssetType,Provider<? extends AssetBuilder>> implements AssetProviderRegistry {

    @Override
    public Provider<? extends AssetBuilder> getService( AssetType assetType ) {
        final Provider<? extends AssetBuilder> result = super.getService( assetType );
        if ( null == result ) {
            throw new NoSuchElementException( "No provider registered for " + assetType );
        }
        return result;
    }
}
