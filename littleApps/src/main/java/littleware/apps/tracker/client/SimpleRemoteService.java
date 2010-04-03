/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.tracker.client;

import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.UUID;
import littleware.apps.tracker.TaskManagerRemote;
import littleware.apps.tracker.TaskQuery;
import littleware.asset.client.SimpleLittleService;
import littleware.base.BaseException;

/**
 *
 * @author pasquini
 */
public class SimpleRemoteService extends SimpleLittleService implements TaskManagerRemoteService {
    private TaskManagerRemote remoteHandle;

    /** For serialization */
    public SimpleRemoteService() {}
    
    public SimpleRemoteService( TaskManagerRemote remoteHandle ) {
        this.remoteHandle = remoteHandle;
    }

    @Override
    public Collection<UUID> runQuery(TaskQuery query) throws BaseException, GeneralSecurityException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
