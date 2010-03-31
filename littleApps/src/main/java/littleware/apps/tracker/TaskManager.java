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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import littleware.base.BaseException;

/**
 * Tool for placing tasks in queue
 */
public interface TaskManager {
    public <T extends Task> T addTaskToQueue( T task, UUID queueId ) throws BaseException,
                  GeneralSecurityException, RemoteException;
    
    /**
     * Shortcut for getAssets( getAssetIdsFrom( task ).values() )
     */
    public List<Task>   getSubtask( UUID task ) throws BaseException, GeneralSecurityException,
            RemoteException;

    /**
     * Load the set of tasks in the given queue that have not yet
     * entered a finished state (Complete, Canceled, ...)
     * in reverse-creation-time order
     *
     * @param queueId id of queue task assigned to
     */
    public TaskSet    loadActiveTaskSet( UUID queueId ) throws BaseException, GeneralSecurityException,
                  RemoteException;
    /**
     * Really an internal method - fascilitates dynamic load of TaskSet data ...
     * @param queueId
     * @param createdBefore
     */
    public TaskSet    loadActiveTaskSet( UUID queueId, Date createdBefore ) throws BaseException, GeneralSecurityException,
                  RemoteException;

    public TaskSet    loadActiveTaskSet( UUID queueId, UUID assignedToUserId ) throws BaseException, GeneralSecurityException,
                  RemoteException;

    public TaskSet    loadActiveTaskSet( UUID queueId, UUID assignedToUserId, Date createdBefore ) throws BaseException, GeneralSecurityException,
                  RemoteException;
    
    public TaskSet    loadTaskSet( UUID queueId, TaskStatus status ) throws BaseException, GeneralSecurityException,
                  RemoteException;

    public TaskSet    loadTaskSet( UUID queueId, TaskStatus status, Date createdBefore ) throws BaseException, GeneralSecurityException,
                  RemoteException;

    public TaskSet    loadTaskSet( UUID queueId, TaskStatus status, UUID assignedToUserId ) throws BaseException, GeneralSecurityException,
                  RemoteException;

    public TaskSet    loadTaskSet( UUID queueId, TaskStatus status, UUID assignedToUserId, Date createdBefore  ) throws BaseException, GeneralSecurityException,
                  RemoteException;

    public TaskSet   loadActiveTaskSubmittedBy( UUID queueId, UUID submitterId ) throws BaseException, GeneralSecurityException,
                  RemoteException;

    public TaskSet   loadActiveTaskSubmittedBy( UUID queueId, UUID submitterId, Date createdBefore ) throws BaseException, GeneralSecurityException,
                  RemoteException;

    public TaskSet    loadTaskSubmittedBy( UUID queueId, TaskStatus status, UUID submitterId ) throws BaseException, GeneralSecurityException,
                  RemoteException;

    public TaskSet    loadTaskSubmittedBy( UUID queueId, TaskStatus status, UUID submitterId, Date createdBefore  ) throws BaseException, GeneralSecurityException,
                  RemoteException;

}
