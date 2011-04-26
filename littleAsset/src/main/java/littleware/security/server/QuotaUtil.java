package littleware.security.server;


import littleware.asset.client.AssetSearchManager;
import littleware.asset.client.AssetManager;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import littleware.asset.*;
import littleware.base.BaseException;
import littleware.security.LittleUser;
import littleware.security.Quota;

/**
 * Factor out quota utility methods to avoid circular dependencies
 * with littleware.security.AccountManager implementations.
 */
public interface QuotaUtil {
    public Quota getQuota(LittleUser user, AssetSearchManager search ) throws BaseException,
            GeneralSecurityException, RemoteException;

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
    public int incrementQuotaCount( LittleUser user, AssetManager accountMgr,
            AssetSearchManager search
            ) throws BaseException,
            GeneralSecurityException, RemoteException;

}
