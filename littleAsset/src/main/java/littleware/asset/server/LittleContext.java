/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.asset.server;

import java.security.GeneralSecurityException;
import java.util.UUID;
import javax.security.auth.Subject;
import littleware.asset.Asset;
import littleware.base.BaseException;
import littleware.base.Option;
import littleware.security.LittlePermission;
import littleware.security.LittleUser;
import littleware.security.auth.LittleSession;

/**
 * Context of a remote method call
 */
public interface LittleContext {
    /**
     * Session call executing under
     */
    public LittleSession getSession();
    /**
     * User associated with session
     */
    public LittleUser    getCaller();
    /**
     * JAAS Subject includes LittleUser and LittleSession credentials,
     * and possibly other credentials too
     */
    public Subject       getSubject();
    
    public LittleTransaction getTransaction();
    public Option<Asset>     checkCache( UUID id );
    public void              addToCache( Asset asset );

    /**
     * Report when an asset has just been saved, so
     * we can check to make sure that we don't repeatedly
     * save the same asset.  Also adds asset to cache.
     */
    public void              savedAsset( Asset asset );
    public Option<Asset>     checkIfSaved( UUID id );

    public boolean isAdmin();
    
    /**
     * Check whether this context's caller has the given permission according to the
     * acl with the given id
     * @param permission
     * @param aclId
     * @return acl.checkPermission( caller, permission )
     */
    public boolean checkPermission(LittlePermission permission, UUID aclId) throws BaseException, GeneralSecurityException;

    //--------------------------

    public interface ContextFactory {
        public LittleContext  build( UUID sessionId );
        public LittleContext  getAdminContext();
    }
}
