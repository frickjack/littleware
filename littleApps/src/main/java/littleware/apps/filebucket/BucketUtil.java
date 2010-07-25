/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.filebucket;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.base.BaseException;
import littleware.base.feedback.Feedback;

/**
 * Utility to simplify interaction with BucketManager
 */
public interface BucketUtil {
    /**
     * Write a block of text to a bucket path
     */
    public <T extends Asset> T writeText ( T asset, String bucketPath,
                                String data, String updateComment,
                                Feedback feedback
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
    public String readText ( UUID assetId, String path, Feedback feedback
                                       ) throws BaseException, GeneralSecurityException,
        RemoteException, IOException;

    public void readToFile( UUID assetId, String bucketPath, File destination
                                       ) throws BaseException, GeneralSecurityException,
        RemoteException, IOException;

    public <T extends Asset> T  writeAll( T asset, String bucketPath,
            byte[] data, String updateComment, Feedback feedback
            ) throws BaseException, GeneralSecurityException,
            RemoteException, IOException;

    public byte[] readAll( UUID assetId, String bucketPath, Feedback feedback
            ) throws BaseException, GeneralSecurityException,
            RemoteException, IOException;

    public <T extends Asset> T writeFile( T asset, String bucketPath,
            File source, String updateComment, Feedback feedback
            ) throws BaseException, GeneralSecurityException,
            RemoteException, IOException;
}
