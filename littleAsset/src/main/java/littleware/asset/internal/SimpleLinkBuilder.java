package littleware.asset.internal;

import littleware.asset.Asset;
import littleware.asset.LinkAsset;
import littleware.asset.LinkAsset.LinkBuilder;
import littleware.asset.spi.AbstractAsset;
import littleware.asset.spi.AbstractAssetBuilder;

public class SimpleLinkBuilder extends AbstractAssetBuilder<LinkAsset.LinkBuilder> implements LinkAsset.LinkBuilder {

    @Override
    public LinkBuilder from(Asset value) {
        return parentInternal( value );
    }

    private static class SimpleLink extends AbstractAsset implements LinkAsset {
        public SimpleLink( SimpleLinkBuilder builder ) {
            super( builder );
        }

        @Override
        public LinkBuilder copy() {
            return (new SimpleLinkBuilder()).copy( this );
        }

    }

    //---------------------------------------------

    public SimpleLinkBuilder() {
        super( LinkAsset.LINK_TYPE );
    }

    @Override
    public LinkAsset build() {
        return new SimpleLink( this );
    }

}
