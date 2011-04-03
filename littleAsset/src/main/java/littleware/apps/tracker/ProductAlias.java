/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.tracker;

import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetBuilder;
import littleware.asset.AssetType;
import littleware.base.Maybe;
import littleware.base.UUIDFactory;

/**
 *
 * @author pasquini
 */
public interface ProductAlias extends Asset {
    /**
     * Alias for getToId - reference to product or product alias
     */
    public UUID getProductId();
    
    @Override
    public PABuilder copy();
    
    public interface PABuilder extends AssetBuilder {
        @Override
        public PABuilder name( String value );
        
        @Override
        public PABuilder parent( Asset value );
        /**
         * Alias for setToId
         */
        public PABuilder product( Product value );
        public PABuilder product( ProductAlias value );
        
        @Override
        public PABuilder copy( Asset source );
        
        @Override
        public ProductAlias build();
    }
    
    public static class Type extends AssetType {
        private Type() {
            super(
            UUIDFactory.parseUUID("1A827A6E61AE45939DE46B62F69B93B2"),
            "littleware.ProductAlias"
            );
        }

        @Override
        public Maybe<AssetType> getSuperType() {
            return Maybe.something( (AssetType) AssetType.LINK );
        }
        
        @Override
        public PABuilder create() {
            return new SimplePABuilder();
        }
    }
    
    public static final Type PAType = new Type();
}
