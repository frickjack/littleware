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
import littleware.apps.tracker.internal.SimpleVABuilder;
import littleware.asset.Asset;
import littleware.asset.AssetBuilder;
import littleware.asset.AssetType;
import littleware.base.Maybe;
import littleware.base.UUIDFactory;



public interface VersionAlias extends Asset {
    /**
     * Alias for getFromId
     */
    public UUID getProductId();
    /**
     * Alias for getToId - returns id of version or version-alias node
     */
    public UUID getVersionId();

    @Override
    public VABuilder copy();
    
    public interface VABuilder extends AssetBuilder {
        @Override
        public VABuilder name( String value );

        /**
         * @throws IllegalArgumentException if value is not a Product
         * @param value product 
         */
        @Override
        public VABuilder parent( Asset value );
        /** Type-safe alias for parent */
        public VABuilder product( Product value );
        /** Id of version to reference - identical to toId */
        public VABuilder version( UUID value );
        public VABuilder version( Version value );
        
        @Override
        public VABuilder copy( Asset source );
        
        @Override
        public VersionAlias build();
    }
    
    public static class Type extends AssetType {
        private Type() {
            super(
            UUIDFactory.parseUUID("1CD26A5FDBD141D2904AACCEC3D0B3F2"),
            "littleware.VersionAlias"
            );
        }

        @Override
        public Maybe<AssetType> getSuperType() {
            return Maybe.something( (AssetType) AssetType.LINK );
        }
        
        @Override
        public VABuilder create() {
            return new SimpleVABuilder();
        }
    }
    
    public static final Type VAType = new Type();
    
}
