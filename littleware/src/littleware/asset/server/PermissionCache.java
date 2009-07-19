/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.server;

import com.google.inject.ImplementedBy;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.security.Permission;
import java.util.UUID;
import littleware.asset.AssetRetriever;
import littleware.base.BaseException;
import littleware.base.Maybe;
import littleware.security.LittlePermission;
import littleware.security.LittlePrincipal;
import littleware.security.LittleUser;

/**
 * Allow AssetSearchManager to safely cache ACL
 * access permissions.  AssetManager informs cache
 * when changes occur to ACL and Group members.
 */
@ImplementedBy(SimplePermissionCache.class)
public interface PermissionCache {
    /**
     * Check if the given principal is an administrator
     */
    public boolean isAdmin( LittleUser user, AssetRetriever search
            ) throws BaseException, RemoteException, GeneralSecurityException;

    /**
     * Check the permission for the given principal against the acl
     * with id uAcl.  If the acl is not yet cached, then load the acl
     * with the retriever.
     *
     * @param principal
     * @param permission
     * @param retriever
     * @param uAcl
     * @return result of acl.checkPermission
     */
    public boolean checkPermission( LittlePrincipal principal, LittlePermission permission,
            AssetRetriever retriever, UUID uAcl
            ) throws BaseException, RemoteException, GeneralSecurityException;

    /**
     * Clear the cache.  The AssetManager should clear the cache
     * whenever there is an update to a group or acl.
     */
    public void clear();
}
