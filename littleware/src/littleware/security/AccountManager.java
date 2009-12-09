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

import java.security.GeneralSecurityException;
import java.rmi.RemoteException;
import java.rmi.Remote;
import java.util.UUID;
import littleware.asset.AssetException;
import littleware.base.BaseException;
import littleware.base.ReadOnly;


/**
 * Interface to a central security manager for managing
 * littleware java.security.Principals.
 * A generic AccountManager implementation may not restrict
 * read-access to littleware Principal data, but should
 * enforce write-access restrictions.
 */
public interface AccountManager extends Remote {

    /** group-name of admin group */
    public static final String LITTLEWARE_ADMIN_GROUP = "group.littleware.administrator";
    /** Admin user name */
    public static final String LITTLEWARE_ADMIN = "littleware.administrator";
    /** group containing everybody */
    public static final String LITTLEWARE_EVERYBODY_GROUP = "group.littleware.everybody";
    /** Admin user id */
    public static final UUID UUID_ADMIN = littleware.base.UUIDFactory.parseUUID("00000000000000000000000000000000");
    /** Admin group id */
    public static final UUID UUID_ADMIN_GROUP = littleware.base.UUIDFactory.parseUUID("89A1CB79B5944447BED9F38D398A7D12");

    /**
     * Increment the quota op-count on all the quotas
     * in the quota-chain associated with the calling user.
     * Internally does a setuid up to administrator to get permissions
     * to update the underlying Quota assets.
     *
     * @return number of operations remaining in the user's quota, -1 indicates no limit
     */
    int incrementQuotaCount() throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;




    /**
     * Get the end of the quota-chain associated with the given user.
     *
     * @return the user's quota, or null if quota of specified type is not
     *                              assigned to the user
     * @exception NoSuchThingException if quota not found
     */
    public Quota getQuota(LittleUser user) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;
}

