/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.security.auth.spi;

import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import littleware.asset.AssetException;
import littleware.asset.client.LittleService;
import littleware.base.BaseException;
import littleware.security.auth.LittleSession;
import littleware.security.auth.ServiceType;
import littleware.security.auth.SessionExpiredException;
import littleware.security.auth.SessionHelper;

/**
 * Under the hood SessionHelper implementation interface
 * has all the same methods, but each method takes an
 * additional mySessionId parameter.
 */
public interface StatelessSessionHelper {
    /**
     * Get the session asset this SessionHelper is associated with
     *
     * @throws SessionExpiredException if the session this helper is
     *          associated with is no longer active
     */
    public LittleSession getSession( UUID mySessionId) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;

    /**
     * Get the AssetManager setup to excute on behalf of this session's principal
     *
     * @param serviceType of the service desired
     * @return the service manager - caller should cast to appropriate type
     * @throws SessionExpiredException if the session this helper is
     *          associated with is no longer active
     */
    public <T extends LittleService> T getService(UUID mySessionId, ServiceType<T> serviceType) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;

    /**
     * Create a new session for this user
     */
    public SessionHelper createNewSession(UUID mySessionId, String sessionComment)
            throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;

    /**
     * Get the server-side version string.
     * Provides mechanism for long-running clients to decide whether
     * they need to restart with a new code base.
     * Just retrieves data string from /littleware.home/ServerVersion asset.
     */
    public String getServerVersion( UUID mySessionId ) throws RemoteException;

}
