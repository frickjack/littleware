package littleware.security.server;

import java.security.*;
import java.rmi.RemoteException;
//import java.rmi.server.UnicastRemoteObject;

import littleware.asset.*;
import littleware.base.*;
import littleware.security.*;

/**
 * RMI remote-ready wrapper around a real implementation.
 * Should usually wrap a SubjectInvocationHandler equiped DynamicProxy
 * of a base implementation class.
 */
public class RmiAccountManager extends LittleRemoteObject implements AccountManager {
    private static final long serialVersionUID = 4552493166286195336L;

    private AccountManager om_proxy = null;

    public RmiAccountManager(AccountManager m_proxy) throws RemoteException {
        //super( littleware.security.auth.SessionUtil.getRegistryPort() );
        om_proxy = m_proxy;
    }


    @Override
    public int incrementQuotaCount() throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return om_proxy.incrementQuotaCount();
    }

    @Override
    public LittleUser createUser(LittleUser p_user,
            String s_password) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return om_proxy.createUser(p_user, s_password);
    }

    @Override
    public LittleUser updateUser(LittleUser p_update, String s_password,
            String s_update_comment) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return om_proxy.updateUser(p_update, s_password, s_update_comment);
    }

    @Override
    public boolean isValidPassword(String s_password) throws RemoteException {
        return om_proxy.isValidPassword(s_password);
    }

    @Override
    public LittleUser getAuthenticatedUser() throws GeneralSecurityException, RemoteException {
        return om_proxy.getAuthenticatedUser();
    }

    @Override
    public Quota getQuota(LittleUser p_user) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return om_proxy.getQuota(p_user);
    }
}// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

