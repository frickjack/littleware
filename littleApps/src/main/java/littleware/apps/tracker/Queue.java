/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.tracker;


import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.base.BaseException;


/**
 * Interface for Queue assets.
 * The task-set and iterator methods are lazy-load wrappers
 * around calls to TaskManager
 */
public interface Queue extends Asset, TaskSet {
    /**
     * Shortcut for getAssets( getAssetIdsFrom( task ).values() )
     */
    public TaskSet   getSubtask() throws BaseException, GeneralSecurityException,
            RemoteException;

    /**
     * Load the set of tasks in the given queue that have not yet
     * entered a finished state (Complete, Canceled, ...)
     * in reverse-creation-time order
     *
     * @param queueId id of queue task assigned to
     */
    public TaskSet    loadActiveTaskSet() throws BaseException, GeneralSecurityException,
                  RemoteException;

    /**
     * Really an internal method - fascilitates dynamic load of TaskSet data ...
     * @param queueId
     * @param createdBefore
     */
    public TaskSet    loadActiveTaskSet( Date createdBefore ) throws BaseException, GeneralSecurityException,
                  RemoteException;

    public TaskSet    loadActiveTaskSet( UUID assignedToUserId ) throws BaseException, GeneralSecurityException,
                  RemoteException;

    public TaskSet    loadActiveTaskSet( UUID assignedToUserId, Date createdBefore ) throws BaseException, GeneralSecurityException,
                  RemoteException;

    public TaskSet    loadTaskSet( TaskStatus status ) throws BaseException, GeneralSecurityException,
                  RemoteException;

    public TaskSet    loadTaskSet( TaskStatus status, Date createdBefore ) throws BaseException, GeneralSecurityException,
                  RemoteException;

    public TaskSet    loadTaskSet( TaskStatus status, UUID assignedToUserId ) throws BaseException, GeneralSecurityException,
                  RemoteException;

    public TaskSet    loadTaskSet( TaskStatus status, UUID assignedToUserId, Date createdBefore  ) throws BaseException, GeneralSecurityException,
                  RemoteException;

    public TaskSet   loadActiveTaskSubmittedBy( UUID submitterId ) throws BaseException, GeneralSecurityException,
                  RemoteException;

    public TaskSet   loadActiveTaskSubmittedBy( UUID submitterId, Date createdBefore ) throws BaseException, GeneralSecurityException,
                  RemoteException;

    public TaskSet    loadTaskSubmittedBy( TaskStatus status, UUID submitterId ) throws BaseException, GeneralSecurityException,
                  RemoteException;

    public TaskSet    loadTaskSubmittedBy( TaskStatus status, UUID submitterId, Date createdBefore  ) throws BaseException, GeneralSecurityException,
                  RemoteException;


}

