package littleware.security.auth.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.GeneralSecurityException;

import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.asset.AssetManager;
import littleware.asset.AssetSearchManager;
import littleware.base.ReadOnlyException;
import littleware.base.DataAccessException;
import littleware.base.NoSuchThingException;
import littleware.base.BaseException;
import littleware.security.auth.*;
import littleware.security.ManagerException;
import littleware.security.AccessDeniedException;

/**
 * Straight forward implementation of SessionHelper - 
 * deploys RMI-enabled managers wrapping timeout/read-only
 * aware proxies of standard Manager implementations.
 */
public class RmiSessionHelper extends UnicastRemoteObject implements SessionHelper {

    SessionHelper om_helper = null;

    /**
     * Constructor sets up a SessionInvocationHandler based wrapper
     * around the given helper implementation.
     */
    public RmiSessionHelper(SessionHelper m_helper) throws RemoteException {
        super( littleware.security.auth.SessionUtil.getRegistryPort() );        
        om_helper = m_helper;
    }

    public LittleSession getSession() throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return om_helper.getSession();
    }

    public <T extends Remote> T getService(ServiceType<T> n_type) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        if ( n_type.equals( ServiceType.SESSION_HELPER ) ) {
            // Need to do this hook when running in same JVM
            return (T) this;
        }
        return om_helper.getService(n_type);
    }

    /**
     * Create a new session for this user
     */
    public SessionHelper createNewSession(String s_session_comment)
            throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return om_helper.createNewSession(s_session_comment);
    }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

