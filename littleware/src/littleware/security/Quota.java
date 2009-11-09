/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security;

import com.google.inject.ImplementedBy;
import java.rmi.RemoteException;
import java.util.UUID;
import java.security.GeneralSecurityException;

import littleware.asset.Asset;
import littleware.asset.AssetBuilder;
import littleware.asset.AssetRetriever;
import littleware.asset.AssetException;
import littleware.base.*;

/**
 * Quota asset gets attached to a user to restrict
 * the user's access to the littleware database in some way.
 */
public interface Quota extends Asset {

    /**
     * Get the op-count associated with the given quota
     */
    public int getQuotaCount();


    /**
     * Get the op-count limit associated with the given quota
     */
    public int getQuotaLimit();


    /** Shortcut for getFromId() */
    public UUID getUserId();

    /** Shortcut for getToId () */
    public UUID getNextInChainId();

    /**
     * Get the next quota in the quota-chain.
     * Shortcut for getTo()
     */
    public Quota getNextInChain(AssetRetriever retriever) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;

    /**
     * Get the user this quota is associated with (note - may be associated
     * with other users via a quota-chain).  Shortcut for getFrom()
     */
    public LittleUser getUser(AssetRetriever retriever) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;

    @Override
    public Builder copy();

    @ImplementedBy(QuotaBuilder.class)
    public interface Builder extends AssetBuilder {

        /**
         * Get the op-count associated with the given quota
         */
        public int getQuotaCount();

        /**
         * Set the op-count associated with the given quota
         */
        public void setQuotaCount(int i_value);

        public Builder quotaCount( int value );

        /**
         * Get the op-count limit associated with the given quota
         */
        public int getQuotaLimit();

        /**
         * Set the op-count limit associated with the given quota
         */
        public void setQuotaLimit(int i_value);

        public Builder quotaLimit( int value );

        /**
         * Shortcut for setQuotaCount ( getQuotaCount () + 1 )
         */
        public Builder incrementQuotaCount();

        @Override
        public Quota build();
    }
}

