package littleware.security.server;


import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import littleware.asset.*;
import littleware.asset.server.LittleContext;
import littleware.asset.server.ServerAssetManager;
import littleware.asset.server.ServerSearchManager;
import littleware.base.BaseException;
import littleware.security.LittleUser;
import littleware.security.Quota;

/**
 * Factor out quota utility methods to avoid circular dependencies
 * with littleware.security.AccountManager implementations.
 */
public interface QuotaUtil {
    public Quota getQuota( LittleContext ctx, LittleUser user, ServerSearchManager search ) throws BaseException,
            GeneralSecurityException;

    /**
     * Utility to increment quota - needs to run with admin
     * privileges (Subject.doAs).
     *
     * @param user to increment quota for
     * @param accountMgr for saving quota update
     * @param search for naving quota chain
     * @return ops left for the user
     * @throws littleware.base.BaseException
     * @throws littleware.asset.AssetException
     * @throws java.security.GeneralSecurityException
     * @throws java.rmi.RemoteException
     */
    public int incrementQuotaCount( LittleContext ctx, LittleUser user, ServerAssetManager accountMgr,
            ServerSearchManager search
            ) throws BaseException,
            GeneralSecurityException;

}
