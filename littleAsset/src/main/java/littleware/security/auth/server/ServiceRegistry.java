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

import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import littleware.asset.client.LittleService;
import littleware.base.BaseException;
import littleware.base.LittleRegistry;
import littleware.security.auth.ServiceType;
import littleware.security.auth.SessionHelper;

/**
 * Registry maps ServiceType to the ServiceFactory
 * that supports the ServiceType
 */
public interface ServiceRegistry extends LittleRegistry<ServiceType<?>,ServiceFactory<?>> {

    /**
     * Specialization of getService call through to
     *     ServiceFactory<T>.createServiceProvider
     * and casts the result to the ServiceType
     *
     * @throws NoSuchThingException if no ServiceProvider registered for the
     *                requested service type
     */
    public <T extends LittleService> T getService( ServiceType<T> servtype, SessionHelper helper ) throws BaseException,
        GeneralSecurityException, RemoteException;
}
