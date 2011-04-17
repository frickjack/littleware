/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.tracker.internal;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.UUID;
import littleware.apps.tracker.TaskManager;
import littleware.apps.tracker.TaskQuery;
import littleware.apps.tracker.TaskQueryManager;
import littleware.apps.tracker.TaskSet;
import littleware.apps.tracker.TaskSet.IdSetBuilder;
import littleware.asset.AssetSearchManager;
import littleware.base.BaseException;

public class SimpleTaskManager implements TaskManager {
    private final TaskQueryManager taskQuery;
    private final AssetSearchManager search;
    private final Provider<IdSetBuilder> taskSetBuilder;

    @Inject
    public SimpleTaskManager( TaskQueryManager taskQuery, AssetSearchManager search,
            Provider<TaskSet.IdSetBuilder> taskSetBuilder ) {
       this.taskQuery = taskQuery;
       this.search = search;
       this.taskSetBuilder = taskSetBuilder;
    }

    @Override
    public TaskSet getSubtask(UUID task) throws BaseException, GeneralSecurityException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TaskSet getInputs(UUID task) throws BaseException, GeneralSecurityException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TaskSet getOutputs(UUID task) throws BaseException, GeneralSecurityException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TaskSet getAllDependencies(UUID task) throws BaseException, GeneralSecurityException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public TaskSet loadTaskSet(TaskQuery query) throws BaseException, GeneralSecurityException, RemoteException {
        return taskSetBuilder.get().build( runQuery( query ) );
    }

    @Override
    public Collection<UUID> runQuery(TaskQuery query) throws BaseException, GeneralSecurityException, RemoteException {
        return taskQuery.runQuery(query);
    }

}
