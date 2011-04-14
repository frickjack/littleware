/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.server.internal;

import com.google.inject.Singleton;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetRetriever;
import littleware.asset.server.PermissionCache;
import littleware.base.BaseException;
import littleware.base.Maybe;
import littleware.security.AccountManager;
import littleware.security.LittleAcl;
import littleware.security.LittleGroup;
import littleware.security.LittlePermission;
import littleware.security.LittlePrincipal;
import littleware.security.LittleUser;
import littleware.security.SecurityAssetType;

/**
 *
 * @author pasquini
 */
@Singleton
public class SimplePermissionCache implements PermissionCache {
    private Map<UUID,LittleAcl> mapAcl = new HashMap<UUID,LittleAcl>();
    private Maybe<LittleGroup>   maybeAdmin = Maybe.empty();

    @Override
    public boolean checkPermission(LittlePrincipal principal, LittlePermission permission, AssetRetriever retriever, UUID uAcl
            ) throws BaseException, RemoteException, GeneralSecurityException {
        if ( null == uAcl ) {
            return false;
        }
        LittleAcl acl;
        synchronized (this) {
            acl = mapAcl.get( uAcl );
        }
        if ( null == acl ) {
            Maybe<Asset> maybe = retriever.getAsset(uAcl);
            if ( (! maybe.isSet())
                    || (! maybe.get().getAssetType().equals( SecurityAssetType.ACL))
                    ) {
                return false;
            }
            acl = maybe.get().narrow();
            synchronized ( this ) {
                mapAcl.put( acl.getId(), acl );
            }
        }
        return acl.checkPermission( principal, permission );
    }

    @Override
    public void clear() {
        mapAcl = new HashMap<UUID,LittleAcl>();
        maybeAdmin = Maybe.empty();
    }

    @Override
    public boolean isAdmin(LittleUser user, AssetRetriever search) throws BaseException, RemoteException, GeneralSecurityException {
        final Maybe<LittleGroup> maybe = maybeAdmin;
        final LittleGroup       group;

        if ( maybe.isSet() ) {
            group = maybe.get();
        } else {
            group = search.getAsset( AccountManager.UUID_ADMIN_GROUP ).get().narrow();
            maybeAdmin = Maybe.something( group );
        }
        return group.isMember(user);
    }

}
