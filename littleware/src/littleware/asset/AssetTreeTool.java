/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset;

import com.google.inject.ImplementedBy;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.UUID;
import littleware.base.BaseException;
import littleware.base.feedback.Feedback;
import littleware.base.TooMuchDataException;

/**
 * Utility methods to help maintain an asset tree
 */
@ImplementedBy(SimpleAssetTreeTool.class)
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
     * @throws java.rmi.RemoteException
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
     * @throws java.rmi.RemoteException
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
     * @throws java.rmi.RemoteException
     */
    public List<Asset> loadBreadthFirst(UUID uRoot, Feedback feedback) throws BaseException,
            GeneralSecurityException, RemoteException, TooMuchDataException;

    public List<Asset> loadBreadthFirst(UUID uRoot, Feedback feedback, int iMaxDepth ) throws BaseException,
            GeneralSecurityException, RemoteException, TooMuchDataException;
}
