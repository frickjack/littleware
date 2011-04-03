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

import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetBuilder;
import littleware.asset.AssetType;
import littleware.base.BaseException;
import littleware.base.UUIDFactory;

/**
 * Product version
 */
public interface Version extends Asset {
    public UUID  getProductId();

    /**
     * Shortcut for search.getFromIds( MemberType )
     */
    public Map<String,UUID> getMembers() throws BaseException, GeneralSecurityException, RemoteException;

    @Override
    public VersionBuilder copy();

    public interface VersionBuilder extends AssetBuilder {
        @Override
        public VersionBuilder name( String value );

        @Override
        public VersionBuilder copy( Asset value );
        /**
         * Throws IllegalArgumentException if parent is not a product
         */
        @Override
        public VersionBuilder parent( Asset value );
        
        /**
         * Alias for parent
         */
        public VersionBuilder product( Product value );
        @Override
        public Version build();
    }
    
    public static class Type extends AssetType {
        private Type() {
            super( UUIDFactory.parseUUID( "4869CDB1FA514055B0363449431A6278" ),
                    "littleware.Version"
                    );
        }
        
        @Override
        public VersionBuilder create() {
            return new SimpleVersionBuilder();
        }
    }
    
    public static final Type VersionType = new Type();
}
