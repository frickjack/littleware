package littleware.asset.server;


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
    public Quota getQuota(LittleUser p_user, AssetSearchManager m_search ) throws BaseException,
            GeneralSecurityException, RemoteException;

    /**
     * Utility to increment quota - needs to run with admin
     * privileges (Subject.doAs).
     *
     * @param p_user to increment quota for
     * @param m_asset for saving quota update
     * @param m_search for naving quota chain
     * @return ops left for the user
     * @throws littleware.base.BaseException
     * @throws littleware.asset.AssetException
     * @throws java.security.GeneralSecurityException
     * @throws java.rmi.RemoteException
     */
    public int incrementQuotaCount( LittleUser p_user, AssetManager m_asset,
            AssetSearchManager m_search
            ) throws BaseException,
            GeneralSecurityException, RemoteException;

}
