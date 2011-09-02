/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.tracker.client;

import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.UUID;
import littleware.apps.tracker.TaskQueryManager;
import littleware.apps.tracker.TaskQuery;
import littleware.base.BaseException;

/**
 *
 * @author pasquini
 */
public class SimpleQueryService implements TaskQueryManagerService {
    private TaskQueryManager remoteHandle;

    /** For serialization */
    public SimpleQueryService() {}
    
    public SimpleQueryService( TaskQueryManager remoteHandle ) {
        this.remoteHandle = remoteHandle;
    }

    @Override
    public Collection<UUID> runQuery(TaskQuery query) throws BaseException, GeneralSecurityException, RemoteException {
        return remoteHandle.runQuery(query);
    }

}
