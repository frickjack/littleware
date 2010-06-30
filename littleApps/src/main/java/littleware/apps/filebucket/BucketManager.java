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
     * @exception BucketException on failure to access bucket file system
     * @exception GeneralSecurityException if user does not have asset READ permission
     */
    public Bucket getBucket ( UUID assetId ) throws BaseException, GeneralSecurityException,
                       AssetException, RemoteException, IOException;
    
    
    /**
     * Save the given asset, and add a file to the bucket or overwrite an existing file.
     * Must have WRITE permissions to the owning asset.
     * May be restricted by the littleware.security.Quota system.
     *
     * @param a_in to save, and under which bucket to store the s_path data 
     * @param s_path within the bucket - some implementations may restrict name
     *                     (ex: no /)
     * @param s_data string based data to store under the path
     * @param s_update_comment to save a_in with
     * @return post-save a_in with new transaction-count and update-comment 
     */
    public <T extends Asset> T writeToBucket ( T a_in, String s_path,
                                String s_data, String s_update_comment
                                ) throws BaseException, GeneralSecurityException,
        AssetException, RemoteException, IOException;
    
    
    /**
     * Save the given asset, and add a file to the bucket or overwrite an existing file.
     * Must have WRITE permissions to the owning asset.
     * May be restricted by the littleware.security.Quota system.
     *
     * @param asset to save, and under which bucket to store the s_path data
     * @param path within the bucket - some implementations may restrict name
     *                     (ex: no /)
     * @param data to save
     * @param updateComment to save a_in with
     * @return post-save a_in with new transaction-count and update-comment 
     */
    public <T extends Asset> T writeToBucket ( T asset, String path,
                                byte[] data, String updateComment
                                ) throws BaseException, GeneralSecurityException,
        AssetException, RemoteException, IOException;

    /**
     * Read the file at the given path under the bucket as UTF-8 encoded text,
     * and return the String.  Must have asset READ access.
     *
     * @param assetId id of the asset that owns the bucket - must have READ permission to asset
     * @param path to file under bucket
     * @return text from file
     */
    public String readTextFromBucket ( UUID assetId, String path
                                       ) throws BaseException, GeneralSecurityException,
        AssetException, RemoteException, IOException;    
    
    /**
     * Read the file at the given path under the bucket,
     * and return the bytes.  Must have asset READ access.
     *
     * @param assetId id of the asset that owns the bucket - must have READ permission to asset
     * @param path to file under bucket
     * @return bytes from file
     */
    public byte[] readBytesFromBucket ( UUID assetId, String path
                                       ) throws BaseException, GeneralSecurityException,
        AssetException, RemoteException, IOException;    
    
    /**
     * Erase the specified file from the bucket.
     * Caller must have WRITE permissions on the bucket&apos;s asset.
     *
     * @param asset asset is saved with update comment - must have WRITE permission
     * @param path may end in * to indicate a prefix match
     * @param updateComment to save a_in with
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


