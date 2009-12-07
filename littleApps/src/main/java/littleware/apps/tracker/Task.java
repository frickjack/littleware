package littleware.apps.tracker;

import java.security.GeneralSecurityException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import littleware.base.BaseException;
import littleware.asset.Asset;
import littleware.asset.AssetSearchManager;
import littleware.asset.AssetException;


/**
 * Just provide an interface for all Tasks to implement
 */
public interface Task extends Asset {    
    public TaskStatus getTaskStatus ();

    /**
     * Set the task status.
     *
     * @exception TaskStatusException if TaskStatus.MERGED.equals ( n_status ) -
     *                must use mergeWithTask to set that
     */
    public void setTaskStatus ( TaskStatus n_status ) throws TaskStatusException;

    /**
     * Get the comments attached to this task
     *
     * @return list of asset ids in reverse date-created order
     */
    public List<UUID> getTaskComments (); 
    
    
    /**
     * Get the subtasks this task has spawned that the caller
     * who loaded this asset has access to.
     * Note that every TASK type asset linking FROM this Task
     * is considered a subtask.
     *
     * @return mapping of task status to list 
     *                of asset ids 
     */
    public Map<TaskStatus,List<UUID>>    getSubtask ();
    

    
    /**
     * If TaskStatus.MERGED, then return the id of the TASK to which this task
     * has been merged due to redundancy or whatever, otherwise return null.
     * Same as getToId if TaskStatus.MERGED.
     */
    public UUID     getTaskIdMergedWith ();
    
    /**
     * Merge with the given task - set TaskStatus.MERGED,
     * or set TaskStatus.IDLE if already has TaskStatus.MERGED status and task_merge is null.
     * Must AssetManager.save () for change to take effect in repository.
     *
     * @param task_merge to merge with, or null to come out of TaskStatus.MERGED state
     */
    public void mergeWithTask ( Task task_merge );
    
    
    /**
     * If TakeStatus.WAITING_ON_TASK, then return the ids of the tasks
     * this task has dependencies on.  Note that a task may not necessarily
     * be waiting on all its subtasks.
     */
    public Map<TaskStatus,List<UUID>> getTaskIdDependingOn ();
    
    /**
     * Return the id of the queue this task is in, or null
     * if not in a queue.
     * Same as getToId if status is not TaskStatus.MERGED.
     */
    public UUID getQueueId ();
    
    /**
     * Add this task to the given queue.
     * Change takes effect after AssetManager.save.
     * 
     * @param q_add to queue onto - object is READONLY -
     *              must reload q_add after saving this Task to
     *              observe the change to q_add&apos;s task-list in the repository.
     * @exception TaskStatusException if TaskStatus.MERGED
     */
    public void addToQueue ( Queue q_add ) throws TaskStatusException;
    
    
    /**
     * Make this task a subtask of the given parent.
     * Change takes effect after AssetManager.save.
     * Has same effect as setFromId.
     *
     * @param task_parent to become subtask of.
     *              must reload task_parent after saving this Task to
     *              observe the change to task_parent&apos;s subtask
     *              list in the repository.     
     */
    public void makeSubtaskOf ( Task task_parent );
    
    /**
     * Convenience method to create a dependency on another Task.
     * The dependency does not take effect until the returned DEPENDENCY
     * type asset is saved.  Saving that Dependency also increments this
     * asset&apos;s transaction-count on the server - so a resync
     * is necessary to see the new dependency in getTaskIdDependingOn.
     *
     * @param task_dependon task to depend on
     * @return asset to save to setup the dependency
     */
    public Dependency addDependency ( Task task_dependon );

    /**
     * Convenience method to create a Comment pointing to this Task.
     * The dependency does not take effect until the returned Coment
     * asset is saved.  Saving that Comment also increments this
     * asset&apos;s transaction-count on the server - so a resync
     * is necessary to see the new dependency in getTaskIdDependingOn.
     *
     * @param s_summary to initialize the Comment with
     * @return asset to save to setup the Comment
     * @exception BaseException if summary does not satisfy the
     *              requirements of Asset.setComment.
     */
    public Comment addComment ( String s_summary ) throws BaseException;

    
    /**
     * Convenience method to get the DEPENDENCY assets
     * linking this Task to the Tasks this depends on.
     * Remove a dependency on a Task by deleting the DEPENDENCY asset
     * that point To the Task to remove.
     * 
     * @param m_search search manager to lookup the DEPENDENCY assets with
     * @return set of DEPENDENCY type Assets to investigate
     */
    public Set<Asset> getDependency ( AssetSearchManager m_search 
                                      ) throws BaseException, AssetException,
        GeneralSecurityException, RemoteException;
     
}


// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

