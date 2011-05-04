package littleware.apps.filebucket.server.internal;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import java.rmi.RemoteException;
//import java.rmi.server.UnicastRemoteObject;

import littleware.base.*;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.apps.filebucket.*;
import littleware.net.LittleRemoteObject;

/**
 * RMI remote-ready wrapper around a real implementation.
 * Should usually wrap a SubjectInvocationHandler equiped DynamicProxy
 * of a base implementation class.
 */
public class RmiBucketManager extends LittleRemoteObject implements BucketManager {

    private final BucketManager bucketMgr;

    public RmiBucketManager(BucketManager proxyMgr) throws RemoteException {
        //super( littleware.security.auth.SessionUtil.getRegistryPort() );
        this.bucketMgr = proxyMgr;
    }

    @Override
    public Bucket getBucket(UUID assetId) throws BaseException, GeneralSecurityException,
            AssetException, RemoteException, BucketException, IOException {
        return bucketMgr.getBucket(assetId);
    }


    @Override
    public <T extends Asset> T writeToBucket(T asset, String bucketPath, 
            byte[] data, long offset, String updateComment
            ) throws BaseException, GeneralSecurityException,
            RemoteException, BucketException, IOException {
        return bucketMgr.writeToBucket(asset, bucketPath, data, offset, updateComment);
    }


    @Override
    public ReadInfo readBytesFromBucket(UUID assetId, String bucketPath, long offset, int maxBuff ) throws BaseException, GeneralSecurityException,
            AssetException, RemoteException, BucketException, IOException {
        return bucketMgr.readBytesFromBucket(assetId, bucketPath, offset, maxBuff );
    }

    @Override
    public <T extends Asset> T eraseFromBucket(T asset, String bucketPath, String updateComment) throws BaseException, GeneralSecurityException,
            AssetException, RemoteException, BucketException, IOException {
        return bucketMgr.eraseFromBucket(asset, bucketPath, updateComment);
    }

    @Override
    public <T extends Asset> T renameFile(T asset, String s_start_path, String s_rename_path,
            String updateComment) throws BaseException, GeneralSecurityException,
            AssetException, RemoteException, BucketException, IOException {
        return bucketMgr.renameFile(asset, s_start_path, s_rename_path, updateComment);
    }

    @Override
    public <T extends Asset> T copyFile(UUID sourceId, String sourcePath,
            T destAsset, String destPath,
            String updateComment) throws BaseException, GeneralSecurityException,
            AssetException, RemoteException, BucketException, IOException {
        return bucketMgr.copyFile(sourceId, sourcePath, destAsset, destPath,
                updateComment);
    }

    @Override
    public Integer getMaxBufferSize() throws RemoteException {
        return bucketMgr.getMaxBufferSize();
    }
}

