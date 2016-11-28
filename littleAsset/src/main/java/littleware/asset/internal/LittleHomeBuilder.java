package littleware.asset.internal;

import littleware.asset.spi.AbstractAsset;
import littleware.asset.spi.AbstractAssetBuilder;
import littleware.asset.LittleHome;

public class LittleHomeBuilder extends AbstractAssetBuilder<LittleHome.HomeBuilder> implements LittleHome.HomeBuilder {

    private static class HomeAsset extends AbstractAsset implements LittleHome {
        public HomeAsset( LittleHomeBuilder builder ) {
            super( builder );
        }

        @Override
        public LittleHome.HomeBuilder copy() {
            return (new LittleHomeBuilder()).copy( this );
        }
    }

    public LittleHomeBuilder () {
        super( LittleHome.HOME_TYPE );
    }

    @Override
    public LittleHome build() {
        return new HomeAsset( this );
    }
}
