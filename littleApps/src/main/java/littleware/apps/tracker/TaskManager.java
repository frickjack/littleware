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

import com.google.inject.ImplementedBy;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.UUID;
import littleware.base.BaseException;

/**
 * Tool for placing tasks in queue
 */
@ImplementedBy(SimpleTaskManager.class)
public interface TaskManager {
    /**
     * Shortcut for getAssets( getAssetIdsFrom( task, TrackerAssetType.TASK ).values() )
     */
    public TaskSet   getSubtask( UUID task ) throws BaseException, GeneralSecurityException,
            RemoteException;

    /**
     * Shortcut for getAssets( getAssetIdsFrom( task, TrackerAssetType.INPUT ).values() ) ...
     */
    public TaskSet  getInputs( UUID task ) throws BaseException,
            GeneralSecurityException, RemoteException;
    
    /**
     * Shortcut for getAssets( getAssetIdsFrom( task, TrackerAssetType.OUTPUT ).values() ) ...
     */
    public TaskSet  getOutputs( UUID task ) throws BaseException,
            GeneralSecurityException, RemoteException;

    /**
     * Shortcut for getAssets( getAssetIdsFrom( task, TrackerAssetType.DEPEND ).values() ) ...
     * note that INPUT and OUTPUT are DEPEND subtypes
     */
    public TaskSet  getAllDependencies( UUID task ) throws BaseException,
            GeneralSecurityException, RemoteException;

    public TaskSet  loadTaskSet( TaskQuery query ) throws BaseException,
            GeneralSecurityException, RemoteException;

    public Collection<UUID>  runQuery( TaskQuery query ) throws BaseException,
            GeneralSecurityException, RemoteException;

}
