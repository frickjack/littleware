package littleware.apps.tracker;

import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import java.util.List;

import littleware.base.BaseException;
import littleware.asset.Asset;


/**
 * Interface for Queue assets.
 * The Task type assets pointing to a queue are the Queue&apos;s members.
 */
public interface Queue extends Asset {
        
    /**
     * Get the ids of tasks in this queue.
     *
     * @param m_tracker to access 
     * @param n_status of tasks to retrieve - null to retrieve all tasks in the queue
     * @return the list of ids of tasks in this queue with the given status that the caller has access to
     */
    public List<UUID> getTask( TaskStatus n_status );    
}


// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

