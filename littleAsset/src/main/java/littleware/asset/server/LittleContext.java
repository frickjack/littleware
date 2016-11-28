package littleware.asset.server;

import java.security.GeneralSecurityException;
import java.util.Optional;
import java.util.UUID;
import javax.security.auth.Subject;
import littleware.asset.Asset;
import littleware.base.BaseException;
import littleware.security.LittlePermission;
import littleware.security.LittleUser;
import littleware.security.auth.LittleSession;

/**
 * Context of a remote method call.
 * Each remote call receives its own context.
 * LittleContext should only be instantiated in
 * remote method marshal code, then passed around
 * through chained calls to local services.
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
    public Optional<Asset>     checkCache( UUID id );
    public void              addToCache( Asset asset );

    /**
     * Report when an asset has just been saved, so
     * we can check to make sure that we don't repeatedly
     * save the same asset.  Also adds asset to cache.
     */
    public void              savedAsset( Asset asset );
    public Optional<Asset>     checkIfSaved( UUID id );

    public boolean isAdmin();
    
    /**
     * Check whether this context's caller has the given permission according to the
     * acl with the given id
     * @param permission
     * @param aclId
     * @return acl.checkPermission( caller, permission )
     */
    public boolean checkPermission(LittlePermission permission, UUID aclId) throws BaseException, GeneralSecurityException;

    /**
     * Get a context with the same transaction state,
     * but with administrator permissions.
     * TODO: setup adminContext to use its own cache too
     */
    public LittleContext getAdminContext();

    //--------------------------

    public interface ContextFactory {
        public LittleContext  build( UUID sessionId );
        public LittleContext  buildAdminContext();
    }
    
}
