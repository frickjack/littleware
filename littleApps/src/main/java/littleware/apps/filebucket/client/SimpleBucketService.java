/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.filebucket.client;

import java.io.IOException;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import littleware.apps.filebucket.Bucket;
import littleware.apps.filebucket.BucketException;
import littleware.apps.filebucket.BucketManager;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.asset.client.AssetLoadEvent;
import littleware.asset.client.LittleService;
import littleware.asset.client.SimpleLittleService;
import littleware.base.BaseException;

public class SimpleBucketService extends SimpleLittleService implements BucketManagerService {

    private static final long serialVersionUID = 8109936535192171146L;
    private BucketManager server;

    /** Just here to support serialization */
    protected SimpleBucketService() {
    }

    /** Inject remote stub this proxies for */
    public SimpleBucketService(BucketManager server) {
        if (server instanceof LittleService) {
            throw new IllegalArgumentException("Attempt to double wrap service");
        }
        this.server = server;
    }

    private final Integer maxBuff = 1024*1024;
    @Override
    public Integer getMaxBufferSize() {
        return maxBuff;
    }

    @Override
    public Bucket getBucket(UUID assetId) throws BaseException, GeneralSecurityException, AssetException, RemoteException, BucketException, IOException {
        return server.getBucket(assetId);
    }


    @Override
    public <T extends Asset> T writeToBucket(T asset, String path, byte[] data, long offset, String updateComment) throws BaseException, GeneralSecurityException, AssetException, RemoteException, BucketException, IOException {
        T result = server.writeToBucket(asset, path, data, offset, updateComment);
        fireServiceEvent( new AssetLoadEvent( this, result ) );
        return result;
    }


    @Override
    public BucketManager.ReadInfo readBytesFromBucket(UUID assetId, String bucketPath, long offset, int max ) throws BaseException, GeneralSecurityException, RemoteException, BucketException, IOException {
        return server.readBytesFromBucket( assetId, bucketPath, offset, max  );
    }

    @Override
    public <T extends Asset> T eraseFromBucket(T a_in, String s_path, String s_update_comment) throws BaseException, GeneralSecurityException, AssetException, RemoteException, BucketException, IOException {
        T result = server.eraseFromBucket(a_in, s_path, s_update_comment);
        fireServiceEvent( new AssetLoadEvent( this, result ) );
        return result;
    }

    @Override
    public <T extends Asset> T renameFile(T a_in, String s_start_path, String s_rename_path, String s_update_comment) throws BaseException, GeneralSecurityException, AssetException, RemoteException, BucketException, IOException {
        T result = server.renameFile(a_in, s_start_path, s_rename_path, s_update_comment);
        fireServiceEvent( new AssetLoadEvent( this, result ) );
        return result;
    }

    @Override
    public <T extends Asset> T copyFile(UUID u_in, String s_in_path, T a_out, String s_copy_path, String s_update_comment) throws BaseException, GeneralSecurityException, AssetException, RemoteException, BucketException, IOException {
        T result = server.copyFile(u_in, s_in_path, a_out, s_copy_path, s_update_comment);
        fireServiceEvent( new AssetLoadEvent( this, result ) );
        return result;
    }
}
