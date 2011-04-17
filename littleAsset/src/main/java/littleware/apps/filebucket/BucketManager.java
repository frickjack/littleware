/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.filebucket;

import java.util.UUID;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.io.IOException;

import littleware.base.BaseException;
import littleware.asset.AssetException;
import littleware.asset.Asset;

/**
 * Interface for associating a directory with an asset,
 * and manipulating files in that directory.
 * For now we do not allow subdirectories, but we may
 * extend the API in the future.
 * Modifications to an asset&apos;s bucket require the caller to
 * save an update to the asset to advance the transaction count,
 * and therefore require WRITE permission on the asset.
 */
public interface BucketManager extends Remote {
    /**
     * Get the bucket associated with the given asset.
     * Every asset has a bucket associated with it.
     * As long as the bucket remains empty, the path
     * to the bucket is arbitrary and changeable.
     * 
     * @throws BucketException on failure to access bucket file system
     * @throws GeneralSecurityException if user does not have asset READ permission
     */
    public Bucket getBucket ( UUID assetId ) throws BaseException, GeneralSecurityException,
                       AssetException, RemoteException, IOException;
    

    /**
     * Return the maximum buffer that can be read or written in a single call.
     * The BucketUtil includes methods to aggregate larger buffers
     * into multiple calls.
     */
    public Integer getMaxBufferSize() throws RemoteException;
    
    /**
     * Save the given asset, and add a file to the bucket or overwrite an existing file.
     * Must have WRITE permissions to the owning asset.
     * May be restricted by the littleware.security.Quota system.
     *
     * @param asset to save, and under which bucket to store the s_path data
     * @param path within the bucket - some implementations may restrict name
     *                     (ex: no /)
     * @param data to save
     * @param offset into the file to save the data to
     * @param updateComment to save a_in with
     * @return post-save a_in with new transaction-count and update-comment 
     */
    public <T extends Asset> T writeToBucket ( T asset, String path,
                                byte[] data, long offset, String updateComment
                                ) throws BaseException, GeneralSecurityException,
        RemoteException, IOException;




    public interface ReadInfo {
        public byte[] getBuffer();
        /**
         * Offset from the beginning of the file -
         * ex: 0 indicates buffer from beginning of file
         * @return
         */
        public long   getOffset();
        /**
         * Total size of the file in bytes
         */
        public long   getSize();
    }
    
    /**
     * Read the file at the given path under the bucket,
     * and return the bytes.  Must have asset READ access.
     *
     * @param assetId id of the asset that owns the bucket - must have READ permission to asset
     * @param path to file under bucket
     * @param offset into the file to pull bytes from
     * @param maxBuffer maximum size of buffer to return
     * @return bytes from file
     */
    public ReadInfo readBytesFromBucket ( UUID assetId, String path, long offset, int maxBuffer
                                       ) throws BaseException, GeneralSecurityException,
        RemoteException, IOException;


    
    /**
     * Erase the specified file from the bucket.
     * Caller must have WRITE permissions on the bucket&apos;s asset.
     *
     * @param asset asset is saved with update comment - must have WRITE permission
     * @param path may end in * to indicate a prefix match
     * @param up    dateComment to save a_in with
     * @return post-save a_in with new transaction-count and update-comment 
     */
    public <T extends Asset> T eraseFromBucket ( T asset, String path, String updateComment
                                  ) throws BaseException, GeneralSecurityException,
        AssetException, RemoteException, IOException;
    
    /**
     * Rename the specified file within its bucket.
     * Must have asset READ and WRITE permission.
     *
     * @param asset to save, and under which bucket to store the s_path data
     * @param startPath
     * @param renamePath to rename s_start_path file to
     * @param updateComment to save a_in with
     * @return post-save a_in with new transaction-count and update-comment 
     */
    public <T extends Asset> T renameFile ( T asset, String startPath, String renamePath,
                              String updateComment
                                  ) throws BaseException, GeneralSecurityException,
        AssetException, RemoteException, IOException;
    
    /**
     * Copy a file - possibly between different asset buckets.
     * Must have READ and WRITE permission to both assets.
     * May not copy over an existing file.
     *
     * @param sourceId to save, and under which bucket to store the s_path data
     * @param s_start_path
     * @param destId to copy to (may be the same as u_in asset)
     * @param destPath to rename s_start_path file to
     * @param updateComment for saving a_out
     * @return post-save a_in with new transaction-count and update-comment 
     */
    public <T extends Asset> T copyFile ( UUID sourceId, String sourcePath,
                            T destId, String destPath,
                            String updateComment
                             ) throws BaseException, GeneralSecurityException,
        AssetException, RemoteException, IOException;

}


