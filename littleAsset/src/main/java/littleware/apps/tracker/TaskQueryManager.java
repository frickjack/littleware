/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.tracker;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.UUID;
import littleware.apps.tracker.client.TaskQueryManagerService;
import littleware.base.BaseException;
import littleware.base.UUIDFactory;
import littleware.security.auth.ServiceType;

/**
 * Remote interface to server-side TaskManager methods.
 * Clients should interact with TaskManager subtype interface -
 * this interface is an implementation detail that may
 * change in the future.
 */
public interface TaskQueryManager extends Remote {
    public static final ServiceType<TaskQueryManagerService> SERVICE_HANDLE =
            new ServiceType<TaskQueryManagerService>(
                UUIDFactory.parseUUID("AAC399084F5448E48EAF75FB6B6D1434"),
                "littleware.TASK_QUERY_SERVICE", TaskQueryManagerService.class
            );
    
    public Collection<UUID>  runQuery( TaskQuery query ) throws BaseException,
            GeneralSecurityException, RemoteException;
}
