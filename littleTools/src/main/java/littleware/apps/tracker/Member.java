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
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetBuilder;
import littleware.asset.AssetType;
import littleware.base.BaseException;
import littleware.base.UUIDFactory;

/**
 * Member nodes associate a data-file with a product version
 */
public interface Member extends Asset {

    /** Alias for getFromId */
    public UUID getVersionId();

    /**
     * Get the size of the member file/directory/zip/whatever in MB
     * that may need to be transferred to load this member.
     * ProductManager.checkin should set this value.
     */
    public float getSizeMB();

    /**
     * Get an index of the files (not directories)
     * in this member's bucket/folder/zip/whatever.
     * Shortcut for ProductManager.getIndex
     */
    public MemberIndex getIndex() throws BaseException, GeneralSecurityException, RemoteException;

    @Override
    public MemberBuilder copy();

    //----------------------------------------------------------------

    public interface MemberBuilder extends AssetBuilder {

        @Override
        public MemberBuilder name(String value);

        @Override
        public MemberBuilder copy(Asset value);

        /**
         * Alias for parent
         */
        public MemberBuilder version(Version value);

        @Override
        public Member build();

        @Override
        public MemberBuilder creatorId(UUID value);

        @Override
        public MemberBuilder lastUpdaterId(UUID value);

        @Override
        public MemberBuilder aclId(UUID value);

        @Override
        public MemberBuilder ownerId(UUID value);

        @Override
        public MemberBuilder comment(String value);

        @Override
        public MemberBuilder lastUpdate(String value);

        @Override
        public MemberBuilder homeId(UUID value);


        @Override
        public MemberBuilder createDate(Date value);

        @Override
        public MemberBuilder lastUpdateDate(Date value);


        @Override
        public MemberBuilder timestamp(long value);

    }

    //----------------------------------------------------------------

    public static final AssetType MEMBER_TYPE = new AssetType(
            UUIDFactory.parseUUID("92081A474DD947CCB02B21AAC5265834"),
            "littleware.Member");

}