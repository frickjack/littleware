package littleware.asset.internal;

import com.google.inject.Singleton;
import littleware.asset.AssetType;
import littleware.asset.AssetSpecializer;
import littleware.asset.AssetSpecializerRegistry;
import littleware.asset.NullAssetSpecializer;
import littleware.base.SimpleLittleRegistry;

/**
 * SimpleLittleRegistry based implementation of AssetSpecializerRegistry
 */
@Singleton
public class SimpleSpecializerRegistry 
        extends SimpleLittleRegistry<AssetType,AssetSpecializer>
        implements AssetSpecializerRegistry {
    private final AssetSpecializer ospecial_default = new NullAssetSpecializer ();
    
    /**
     * Override SimpleLittleRegistry to return default NullAssetSpecializer
     * if an AssetSpecializer is not registered for the requested key
     * 
     * @param key AssetType to retrieve the specializer for
     * @return specializer for key or default noop specializer
     */
    @Override
    public AssetSpecializer getService( AssetType key ) {
        AssetSpecializer special = super.getService( key );
        if ( null != special ) {
            return special;
        }
        return ospecial_default;
    }
}
