package littleware.apps.filebucket.server;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import java.rmi.RemoteException;
//import java.rmi.server.UnicastRemoteObject;

import littleware.base.*;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.apps.filebucket.*;

/**
 * RMI remote-ready wrapper around a real implementation.
 * Should usually wrap a SubjectInvocationHandler equiped DynamicProxy
 * of a base implementation class.
 */
public class RmiBucketManager extends LittleRemoteObject implements BucketManager {

    private BucketManager om_proxy = null;

    public RmiBucketManager(BucketManager m_proxy) throws RemoteException {
        //super( littleware.security.auth.SessionUtil.getRegistryPort() );
        om_proxy = m_proxy;
    }

    public Bucket getBucket(UUID u_asset) throws BaseException, GeneralSecurityException,
            AssetException, RemoteException, BucketException, IOException {
        return om_proxy.getBucket(u_asset);
    }

    public <T extends Asset> T writeToBucket(T a_in, String s_path,
            String s_data, String s_update_comment) throws BaseException, GeneralSecurityException,
            AssetException, RemoteException, BucketException, IOException {
        return om_proxy.writeToBucket(a_in, s_path, s_data, s_update_comment);
    }

    public <T extends Asset> T writeToBucket(T a_in, String s_path,
            byte[] v_data, String s_update_comment) throws BaseException, GeneralSecurityException,
            AssetException, RemoteException, BucketException, IOException {
        return om_proxy.writeToBucket(a_in, s_path, v_data, s_update_comment);
    }

    public String readTextFromBucket(UUID u_asset, String s_path) throws BaseException, GeneralSecurityException,
            AssetException, RemoteException, BucketException, IOException {
        return om_proxy.readTextFromBucket(u_asset, s_path);
    }

    public byte[] readBytesFromBucket(UUID u_asset, String s_path) throws BaseException, GeneralSecurityException,
            AssetException, RemoteException, BucketException, IOException {
        return om_proxy.readBytesFromBucket(u_asset, s_path);
    }

    public <T extends Asset> T eraseFromBucket(T a_in, String s_path, String s_update_comment) throws BaseException, GeneralSecurityException,
            AssetException, RemoteException, BucketException, IOException {
        return om_proxy.eraseFromBucket(a_in, s_path, s_update_comment);
    }

    public <T extends Asset> T renameFile(T a_in, String s_start_path, String s_rename_path,
            String s_update_comment) throws BaseException, GeneralSecurityException,
            AssetException, RemoteException, BucketException, IOException {
        return om_proxy.renameFile(a_in, s_start_path, s_rename_path, s_update_comment);
    }

    public <T extends Asset> T copyFile(UUID u_in, String s_in_path,
            T a_out, String s_copy_path,
            String s_update_comment) throws BaseException, GeneralSecurityException,
            AssetException, RemoteException, BucketException, IOException {
        return om_proxy.copyFile(u_in, s_in_path, a_out, s_copy_path,
                s_update_comment);
    }
}

