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

import java.util.Date;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetType;
import littleware.asset.LinkAsset;
import littleware.base.Maybe;
import littleware.base.UUIDFactory;

/**
 * Alias for Member node
 */
public interface MemberAlias extends LinkAsset {

    /** Alias for getFromId */
    public UUID getVersionId();

    /** Alias for getToId */
    public UUID getMemberId();

    @Override
    public MABuilder copy();

    public interface MABuilder extends LinkAsset.LinkBuilder {

        @Override
        public MABuilder name(String value);

        /** Alias for parent */
        public MABuilder version(Version parent);

        /** Alias for toId */
        public MABuilder member(Member member);

        @Override
        public MABuilder copy(Asset value);

        @Override
        public MemberAlias build();

        @Override
        public MABuilder creatorId(UUID value);

        @Override
        public MABuilder lastUpdaterId(UUID value);

        @Override
        public MABuilder aclId(UUID value);

        @Override
        public MABuilder ownerId(UUID value);

        @Override
        public MABuilder comment(String value);

        @Override
        public MABuilder lastUpdate(String value);

        @Override
        public MABuilder homeId(UUID value);


        @Override
        public MABuilder createDate(Date value);

        @Override
        public MABuilder lastUpdateDate(Date value);


        @Override
        public MABuilder timestamp(long value);

        @Override
        public MABuilder fromId( UUID value );
        @Override
        public MABuilder from( Asset value );


    }

    public static final AssetType MAType = new AssetType(UUIDFactory.parseUUID("A58AB57363464BB09D31F312E6FE81D5"),
            "littleware.MemberAlias", LinkAsset.LINK_TYPE );
}
