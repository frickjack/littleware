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
import java.util.Date;
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

    //-------------------------------------------------------------

    public interface VersionBuilder extends AssetBuilder {
        @Override
        public VersionBuilder name( String value );

        @Override
        public VersionBuilder copy( Asset value );
        
        /**
         * Alias for parent
         */
        public VersionBuilder product( Product value );
        @Override
        public Version build();

        @Override
        public VersionBuilder creatorId(UUID value);

        @Override
        public VersionBuilder lastUpdaterId(UUID value);

        @Override
        public VersionBuilder aclId(UUID value);

        @Override
        public VersionBuilder ownerId(UUID value);

        @Override
        public VersionBuilder comment(String value);

        @Override
        public VersionBuilder lastUpdate(String value);

        @Override
        public VersionBuilder homeId(UUID value);


        @Override
        public VersionBuilder createDate(Date value);

        @Override
        public VersionBuilder lastUpdateDate(Date value);


        @Override
        public VersionBuilder timestamp(long value);

    }

    //-------------------------------------------------------------

    public static final AssetType VERSION_TYPE = new AssetType( UUIDFactory.parseUUID( "4869CDB1FA514055B0363449431A6278" ),
                    "littleware.Version" );
}
