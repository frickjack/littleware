/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.tracker;

import java.security.GeneralSecurityException;
import java.rmi.RemoteException;
import java.util.*;

import littleware.base.BaseException;
import littleware.base.AssertionFailedException;
import littleware.base.FactoryException;
import littleware.base.UUIDFactory;
import littleware.asset.*;


/**
 * Simple implementation of Task
 */
public class SimpleTask extends SimpleAsset implements Task {
    private List<UUID>                  ov_comments = null;
    private Map<TaskStatus,List<UUID>>  ov_subtask = null;
    private Map<TaskStatus,List<UUID>>  ov_wait4task = null;
    {
        clearTaskData ();
    }
    
    /**
     * Internal utility to clear out the task and comment data
     */
    private void clearTaskData () {
        ov_comments = new ArrayList<UUID> ();
        ov_subtask = new EnumMap<TaskStatus,List<UUID>> ( TaskStatus.class );
        ov_wait4task = new EnumMap<TaskStatus,List<UUID>> ( TaskStatus.class );
    
        for ( TaskStatus n_status : TaskStatus.values () ) {
            ov_subtask.put ( n_status, new ArrayList<UUID> () );
            ov_wait4task.put ( n_status, new ArrayList<UUID> () );
        }
    }
    
    
	
	public SimpleTask () {
        setAssetType ( TrackerAssetType.TASK );
    }
        
    @Override
    public TaskStatus getTaskStatus () {
        return TaskStatus.values ()[ this.getState() ];
    }
    
    @Override
    public void setTaskStatus ( TaskStatus n_status ) {
        this.setState ( n_status.ordinal () );
    }
    
    @Override
    public List<UUID> getTaskComments () {
        return ov_comments;
    }
    
    @Override
    public Map<TaskStatus,List<UUID>>    getSubtask () {
        return ov_subtask;
    }
    
    
    

    @Override
    public UUID     getTaskIdMergedWith () {
        if ( ! TaskStatus.MERGED.equals ( getTaskStatus () ) ) {
            return null;
        }
        return this.getToId ();
    }
                        
    

    public Map<TaskStatus,List<UUID>> getTaskIdWaitingOn ()
    {
        if ( ! TaskStatus.WAITING_ON_TASK.equals ( getTaskStatus () ) ) {
            return null;
        }
        return ov_wait4task;
    }
    
        
    

    @Override
    public UUID getQueueId () {
        if ( (! TaskStatus.WAITING_IN_Q.equals ( getTaskStatus () ))
             && (! TaskStatus.PROCESSING.equals ( getTaskStatus () ))
             ) {
            return null;
        }
        return this.getToId ();
    }
    

    @Override
    public void makeSubtaskOf ( Task task_parent ) {
        setFromId ( task_parent.getObjectId () );
    }

    @Override
    public void addToQueue ( Queue q_add ) throws TaskStatusException {
        if ( TaskStatus.MERGED.equals ( getTaskStatus () ) ) {
            throw new TaskStatusException ( "Man not at MERGED task to a queue" );
        }
        setToId ( q_add.getObjectId () );
    }

    @Override
    public Map<TaskStatus,List<UUID>> getTaskIdDependingOn () {
        return ov_wait4task;
    }

    @Override
    public void mergeWithTask ( Task task_merge ) {
        setToId ( task_merge.getObjectId () );
        setTaskStatus ( TaskStatus.MERGED );
    }
    
    @Override
    public SimpleTask clone () {
        SimpleTask task_clone = (SimpleTask) super.clone ();
        task_clone.clearTaskData ();
        return task_clone;
    }
    
    @Override
    public void sync ( Asset a_other ) {
        if ( this == a_other ) {
            return;
        }
        super.sync ( a_other );
        clearTaskData ();
        SimpleTask task_other = (SimpleTask) a_other;
        ov_comments.addAll ( task_other.ov_comments );
        for ( TaskStatus n_status : TaskStatus.values () ) {
            ov_subtask.get ( n_status ).addAll ( task_other.ov_subtask.get( n_status ) );
            ov_wait4task.get ( n_status ).addAll ( task_other.ov_wait4task.get( n_status ) );
        }
    }
    
    @Override
    public Dependency addDependency ( Task task_dependon ) { 
        try {
            Dependency depend_new = TrackerAssetType.DEPENDENCY.create ();
            depend_new.setFromId ( this.getObjectId () );
            depend_new.setToId ( task_dependon.getObjectId () );
            depend_new.setAclId ( this.getAclId () );
            depend_new.setHomeId ( this.getHomeId () );
            depend_new.setName ( "depend_" + UUIDFactory.makeCleanString ( task_dependon.getObjectId () ) );
            
            return depend_new;
        } catch ( FactoryException e ) {
            throw new AssertionFailedException ( "Unexpected asset-create problem", e );
        }
    }
    
    
    @Override
    public Comment addComment ( String s_summary ) throws BaseException {
        try {
            Comment comment_new = TrackerAssetType.COMMENT.create ();
            comment_new.setToId ( this.getObjectId () );
            comment_new.setAclId ( this.getAclId () );
            comment_new.setHomeId ( this.getHomeId () );
            comment_new.setName ( "cmnt_" + UUIDFactory.makeCleanString ( comment_new.getObjectId () ) );
            comment_new.setSummary ( s_summary );
            
            return comment_new;
        } catch ( FactoryException e ) {
            throw new AssertionFailedException ( "Unexpected asset-create problem", e );
        }        
    }
    

    @Override
    public Set<Asset> getDependency ( AssetSearchManager m_search 
                                      ) throws BaseException, AssetException,
        GeneralSecurityException, RemoteException
    {
        return new HashSet<Asset>(
                m_search.getAssets ( m_search.getAssetIdsFrom ( this.getObjectId (),
                                                                 TrackerAssetType.DEPENDENCY 
                                                                 ).values ()
                                     )
                                     );
    }
    
    
}

