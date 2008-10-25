package littleware.apps.tracker;

import java.security.GeneralSecurityException;
import java.rmi.RemoteException;
import java.util.*;

import littleware.base.BaseException;
import littleware.base.AssertionFailedException;
import littleware.asset.*;


/**
 * Simple implementation of Queue
 */
public class SimpleQueue extends SimpleAsset implements Queue {
    private Map<TaskStatus,List<UUID>>  ov_task = null;
    {
        clearQData ();
    }

    
    /**
     * Internal utility resets the task map
     */
    private void clearQData () {
        ov_task = new EnumMap<TaskStatus,List<UUID>> ( TaskStatus.class );

        for ( TaskStatus n_status : TaskStatus.values () ) {
            ov_task.put ( n_status, new ArrayList<UUID> () );
        }
    }
	
	public SimpleQueue () {
        setAssetType ( TrackerAssetType.QUEUE );
    }
    
    public List<UUID> getTask( TaskStatus n_status 
                               ) 
    {
        if ( null != n_status ) {
            return ov_task.get( n_status );
        } else {
            List<UUID> v_result = new ArrayList<UUID> ();
            for ( List<UUID> v_status : ov_task.values () ) {
                v_result.addAll ( v_status );
            }
            return v_result;
        }
    }
    
    public SimpleQueue clone () {
        SimpleQueue q_clone = (SimpleQueue) super.clone ();
        q_clone.clearQData ();
        return q_clone;
    }
    
    public void sync ( Asset a_other ) throws InvalidAssetTypeException {
        if ( this == a_other ) {
            return;
        }
        super.sync ( a_other );
        SimpleQueue q_other = (SimpleQueue) a_other;
        for ( TaskStatus n_status : TaskStatus.values () ) {
            ov_task.get ( n_status ).addAll ( q_other.ov_task.get( n_status ) );
        }
    }

        
}


// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

