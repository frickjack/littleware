/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security.auth;

import java.rmi.RemoteException;
import javax.security.auth.Subject;
import java.security.GeneralSecurityException;
import java.util.Date;

import littleware.asset.Asset;
import littleware.asset.AssetBuilder;
import littleware.asset.AssetRetriever;
import littleware.asset.AssetException;
import littleware.asset.AssetType;
import littleware.base.BaseException;
import littleware.base.UUIDFactory;

/**
 * Specialization of Asset for session-tracking.
 * The user the session is associated with is the session creator.
 */
public interface LittleSession extends Asset {

    /**
     * Is this a read-only user session (0 != getValue()) ?
     */
    public boolean isReadOnly();
    /**
     * When the session expires
     */
    public Date getEndDate();

    /**
     * Convenience method for getAsset ( getCreator () )...
     */
    public Subject getSubject(AssetRetriever retriever) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;

    @Override
    public Builder copy();



    /** SESSION asset type */
    public static final AssetType SESSION_TYPE = new AssetType(
            UUIDFactory.parseUUID("7AC8C92F30C14AD89FA82DB0060E70C2"),
            "littleware.SESSION");

    //----------------------------------------------------------------

    public interface Builder extends AssetBuilder {

        /**
         * Mark this session read-only (setValue(1)) - must save() this
         * session asset for the change to take effect.
         */
        public void setReadOnly(boolean value);
        public Builder readOnly( boolean value );
        public boolean isReadOnly();

        public Date getEndDate();
        public void setEndDate( Date value );
        public Builder endDate( Date value );


        @Override
        public Builder copy(Asset source);

        @Override
        public LittleSession build();
    }
}

