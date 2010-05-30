/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.security.auth.server;

import littleware.security.auth.*;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;

import littleware.asset.client.LittleService;
import littleware.base.BaseException;

/**
 * Interface for service-provider factory by which new
 * ServiceTypes can inject provider classes into
 * the littleware.security.auth.SessionHelper#getService
 * on the SERVER side - a client should never need to use this.
 */
public interface ServiceFactory<T extends LittleService> {
    /**
     * Factory returns a Remote-reference ready for the
     * server to send back to the client.
     * Performs security check to verify the client associated
     * with m_helper has permission to access the underlying service.
     *
     * @param m_helper session handle
     */
    public T createServiceProvider ( SessionHelper m_helper ) throws BaseException, 
        GeneralSecurityException, RemoteException;
}


