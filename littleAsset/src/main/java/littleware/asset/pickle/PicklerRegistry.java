package littleware.asset.pickle;

import com.google.inject.Provider;
import littleware.asset.Asset;
import littleware.asset.AssetType;

/**
 * Interface for registering and retrieving pickler providers
 */
public interface PicklerRegistry<T extends PickleMaker<Asset>> extends Provider<T> {

    public void registerSpecializer(AssetType assetType,
            Provider<? extends T> provideSpecial);
}
