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

import littleware.apps.tracker.internal.SimpleMABuilder;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetBuilder;
import littleware.asset.AssetType;
import littleware.base.UUIDFactory;

/**
 * Alias for Member node
 */
public interface MemberAlias extends Asset {
    /** Alias for getFromId */
    public UUID getVersionId();
    /** Alias for getToId */
    public UUID getMemberId();
    @Override
    public MABuilder copy();
    
    public interface MABuilder extends AssetBuilder {
        @Override
        public MABuilder name( String value );

        /**
         * IllegalArgumentException if parent is not a Version
         */
        @Override
        public MABuilder parent( Asset parent );
        /** Alias for parent */
        public MABuilder version( Version parent );
        /** Alias for toId */
        public MABuilder member( Member member );
        @Override
        public MABuilder copy( Asset value );
        @Override
        public MemberAlias build();
    }
    
    public static class Type extends AssetType {
        private Type() {
            super( UUIDFactory.parseUUID( "A58AB57363464BB09D31F312E6FE81D5" ),
                    "littleware.MemberAlias"
                    );
        }
        @Override
        public MABuilder create() {
            return new SimpleMABuilder();
        }
    }
    
    public static final Type MAType = new Type();
}
