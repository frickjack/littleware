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

import java.util.*;

import littleware.asset.*;


/**
 * Simple implementation of Queue
 */
public class SimpleQueue extends SimpleAssetBuilder implements Queue {
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
    
    @Override
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
    
    @Override
    public SimpleQueue clone () {
        SimpleQueue q_clone = (SimpleQueue) super.clone ();
        q_clone.clearQData ();
        return q_clone;
    }
    
    @Override
    public void sync ( Asset a_other ) {
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


