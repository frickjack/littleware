package littleware.asset.client;

import java.security.GeneralSecurityException;
import java.util.List;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.client.RemoteException;
import littleware.base.BaseException;
import littleware.base.feedback.Feedback;
import littleware.base.TooMuchDataException;

/**
 * Utility methods to help maintain an asset tree
 */
public interface AssetTreeTool {

    /**
     * Load the assets rooted at uRoot.  Throw an exception on
     * failure to load any children.
     *
     * @param uRoot first asset to load
     * @return breadth-first list with root as first element safe to copy
     *                 in list order or delete in reverse order
     * @throws littleware.base.BaseException
     * @throws java.security.GeneralSecurityException
     * @throws littleware.asset.client.RemoteException
     */
    public List<Asset> loadBreadthFirst(UUID uRoot) throws BaseException,
            GeneralSecurityException, RemoteException, TooMuchDataException;

    /**
     * Load the assets rooted at uRoot.  Throw an exception on
     * failure to load any children.
     *
     * @param uRoot first asset to load
     * @param iMaxDepth to stop tree traversal at
     * @return breadth-first list with root as first element safe to copy
     *                 in list order or delete in reverse order
     * @throws littleware.base.BaseException
     * @throws java.security.GeneralSecurityException
     * @throws littleware.asset.client.RemoteException
     */
    public List<Asset> loadBreadthFirst(UUID uRoot, int iMaxDepth) throws BaseException,
            GeneralSecurityException, RemoteException, TooMuchDataException;

    /**
     * Breadth first load with feedback
     *
     * @param uRoot first asset to load
     * @param feedback provide feedback on progress as load assets
     * @return breadth-first list with root as first element safe to copy
     *                 in list order or delete in reverse order
     * @throws littleware.base.BaseException
     * @throws java.security.GeneralSecurityException
     * @throws littleware.asset.client.RemoteException
     */
    public List<Asset> loadBreadthFirst(UUID uRoot, Feedback feedback) throws BaseException,
            GeneralSecurityException, RemoteException, TooMuchDataException;

    public List<Asset> loadBreadthFirst(UUID uRoot, Feedback feedback, int iMaxDepth ) throws BaseException,
            GeneralSecurityException, RemoteException, TooMuchDataException;
}
