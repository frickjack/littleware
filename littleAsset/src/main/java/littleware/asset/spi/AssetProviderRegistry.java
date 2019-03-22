package littleware.asset.spi;

import com.google.inject.Provider;
import littleware.asset.AssetBuilder;
import littleware.asset.AssetType;
import littleware.base.LittleRegistry;

/**
 * Registry maps AssetType to object factory
 */
public interface AssetProviderRegistry extends LittleRegistry<AssetType,Provider<? extends AssetBuilder>> {

}
