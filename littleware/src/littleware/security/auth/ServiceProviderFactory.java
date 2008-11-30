package littleware.security.auth;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;

import littleware.asset.AssetException;
import littleware.base.BaseException;

/**
 * Interface for service-provider factory by which new
 * ServiceTypes can inject provider classes into
 * the littleware.security.auth.SessionHelper#getService
 * on the SERVER side - a client should never need to use this.
 */
public interface ServiceProviderFactory<T> {
    /**
     * Factory returns a Remote-reference ready for the
     * server to send back to the client.
     * Performs security check to verify the client associated
     * with m_helper has permission to access the underlying service.
     *
     * @param m_helper session handle
     */
    public T createServiceProvider ( SessionHelper m_helper ) throws BaseException, AssetException,
        GeneralSecurityException, RemoteException;
}


// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

