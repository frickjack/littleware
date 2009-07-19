/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.tracker.server;

import littleware.asset.server.NullAssetSpecializer;
import com.google.inject.Inject;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.*;

import littleware.apps.tracker.*;
import littleware.asset.*;
import littleware.base.BaseException;


/**
 * AssetSpecializer for littleware.apps.tracker assets.
 */
public class SimpleTrackerManager extends NullAssetSpecializer {
    private final static Logger       olog_generic = Logger.getLogger ( "littleware.apps.tracker.server.SimpleTrackerManager" );
    
    private final AssetManager        om_asset;
    private final AssetSearchManager  om_search;

    @Inject
	public SimpleTrackerManager ( AssetManager m_asset, 
								  AssetSearchManager m_searcher ) {
		om_asset = m_asset;
		om_search = m_searcher;
	}
    
        
    @Override
    public <T extends Asset> T narrow ( T a_in, AssetRetriever m_retriever
						  ) throws BaseException, AssetException, 
	GeneralSecurityException, RemoteException
    {
        if ( a_in.getAssetType ().equals ( TrackerAssetType.COMMENT ) ) {
            return a_in;
        }
        if ( a_in instanceof Task ) {
            Task          task_in = (Task) a_in;
            
            final List<Task> v_children = new ArrayList<Task>();

            for ( Asset scan : om_search.getAssets ( om_search.getAssetIdsFrom ( a_in.getObjectId (),
                                                              TrackerAssetType.TASK ).values ()
                                                         )
                                                         ) {
                v_children.add( scan.narrow( Task.class ) );
            }
            Collections.sort ( v_children, new Comparator<Task> () {
                @Override
                public int compare ( Task task_a, Task task_b ) {
                    return task_a.getCreateDate ().compareTo ( task_b.getCreateDate () );
                }
            }
                               );
            
            Map<TaskStatus,List<UUID>>  v_subtask = task_in.getSubtask ();
            for ( Task task_entry : v_children ) {
                v_subtask.get ( task_entry.getTaskStatus () ).add ( task_entry.getObjectId () );
            }
            
            final Set<UUID>  v_depends = new HashSet<UUID> ();
            
            for ( Asset a_depend : om_search.getAssets ( om_search.getAssetIdsFrom ( a_in.getObjectId (),
                                                                           TrackerAssetType.DEPENDENCY
                                                                           ).values ()
                                               )
                                               ) {
                if ( a_depend.getToId () != null ) {
                    v_depends.add ( a_depend.getToId () );
                } else {
                    try {
                        om_asset.deleteAsset ( a_depend.getObjectId (), "deleting superfulous dependency" );
                    } catch ( Exception e ) {
                        olog_generic.log ( Level.WARNING, "Failure cleaning up extra depend: " + e );
                    }
                }
            }

            final Map<TaskStatus,List<UUID>> v_waiting4 = task_in.getTaskIdDependingOn ();

            for ( Asset a_entry : om_search.getAssets ( v_depends ) ) {
                Task       task_entry = (Task) a_entry;
                
                v_waiting4.get ( task_entry.getTaskStatus () ).add ( task_entry.getObjectId () );
            }
            
            
            final Set<UUID> v_comments = om_search.getAssetIdsTo ( a_in.getObjectId (),
                                                             TrackerAssetType.COMMENT 
                                                             );
            olog_generic.log ( Level.FINE, "Adding " + v_comments.size () + " comments to Task " +
                               task_in.getName ()
                               );
            task_in.getTaskComments ().addAll ( v_comments );
        }
        if ( a_in.getAssetType ().equals ( TrackerAssetType.QUEUE ) ) {
            littleware.apps.tracker.Queue q_in = a_in.narrow(littleware.apps.tracker.Queue.class);
            for ( Asset a_task : om_search.getAssets ( om_search.getAssetIdsTo ( a_in.getObjectId (),
                                                                                      TrackerAssetType.TASK )
                                                          ) ) {
                q_in.getTask ( ((Task) a_task).getTaskStatus () ).add ( a_task.getObjectId () );
            }
        }
        return a_in;
    }

    @Override
    public void postCreateCallback ( Asset a_new, AssetManager m_asset  							   
                                     ) throws BaseException, AssetException, 
    GeneralSecurityException, RemoteException
    {
        if ( TrackerAssetType.COMMENT.equals ( a_new.getAssetType () ) ) {
            updateIfExists ( a_new.getToId (), TrackerAssetType.TASK,
                             a_new.getComment ()
                             );
        }
        if ( a_new instanceof Task ) {

            updateIfExists ( a_new.getToId (), TrackerAssetType.QUEUE,
                                 "change to task in queue: " + a_new.getName ()
                                 );
                
            updateIfExists ( a_new.getFromId (), TrackerAssetType.TASK,
                                 "change to subtask: " + a_new.getName ()
                             );
        }
        if ( TrackerAssetType.DEPENDENCY.equals ( a_new.getAssetType () ) ) {
            updateIfExists ( a_new.getFromId (), TrackerAssetType.TASK,
                             "new dependency: " + a_new.getName ()
                             );
        }            

    }
        
    /**
     * Internal utility.
     * Update the asset with the given id and AssetType if it exists by
     * simply loading then saviing it with the given update comment,
     * otherwise do nothing.
     *
     * @param u_id of asset to update - NOOP if null
     * @param n_type of asset to update - may be null as wild card
     * @param s_update comment to save the asset with
     * @return true if update issued, false if no asset found
     */
    private boolean updateIfExists ( UUID u_id, AssetType n_type, String s_update
                                  ) throws BaseException, AssetException, 
        GeneralSecurityException, RemoteException
    {
        if ( null == u_id ) {
            return false;
        }
        Asset a_save = om_search.getAsset ( u_id ).getOr( null );
        if ( (null != a_save)
             && ( (null == n_type)
                  || a_save.getAssetType ().equals ( n_type )
                  )
             ) {
            om_asset.saveAsset ( a_save, s_update );
            olog_generic.log ( Level.FINE, "Updated asset as sideffect: " + a_save );
            return true;
        }
        return false;
    }
    
    /**
     * Internal utility.
     * Update the assets referenced by the given before and after ids
     * if the before and after ids are not equal - handle null correctly.
     *
     * @param u_before_id of asset to update before whatever opp is making the call - may be null
     * @param u_after_id of asset to update after whatever opp is making the call - may be null
     * @param n_type of asset to update - may be null as wild card - only
     *                    update referenced asset if it matches this type
     * @param s_update comment to save the before and after assets with
     * @return 1 if u_before_id save attempted, 0 if nothing saved, 2 if u_after is saved, 3 if both saved
     */
    private int updateIfChanged ( UUID u_before_id, 
                                  UUID u_after_id,
                                  AssetType n_type, String s_update
                                     ) throws BaseException, AssetException, 
        GeneralSecurityException, RemoteException
    {
        int i_result = 0;
        
        if ( null != u_after_id ) {
            if ( (null == u_before_id)
                 || (! u_before_id.equals ( u_after_id ))
                 ) {  // save current ToId
                updateIfExists ( u_after_id,
                                     n_type, s_update 
                                 );
                
                i_result += 2;
            }
            if ( (null != u_before_id)
                 && (! u_before_id.equals ( u_after_id ))
                 ) { // save former ToId
                updateIfExists ( u_before_id,
                                 n_type,
                                 s_update
                                 );
                i_result += 1;
            }
        } else if ( null != u_before_id ) {
            updateIfExists ( u_before_id,
                             n_type, s_update
                             );                
            i_result += 1;
        }
        return i_result;
    }
    
        

    @Override
    public void postUpdateCallback ( Asset a_pre_update, Asset a_now, AssetManager m_asset 
                                     ) throws BaseException, AssetException, 
    GeneralSecurityException, RemoteException
    {
        if ( a_now.getAssetType ().equals ( TrackerAssetType.COMMENT ) ) {
            updateIfChanged ( a_pre_update.getToId (),
                              a_now.getToId (),
                              TrackerAssetType.TASK,
                              a_now.getComment ()
                              );
        }

        if ( a_now instanceof Task ) {
            // Update FROM asset transaction-count
            updateIfChanged ( a_pre_update.getFromId (),
                              a_now.getFromId (),
                              TrackerAssetType.TASK,
                              "subtask updated: " + a_now.getName ()
                              );
            updateIfChanged ( a_pre_update.getToId (),
                              a_now.getToId (),
                              TrackerAssetType.QUEUE,
                              "task updated: " + a_now.getName ()
                              );
            
        }
        
    }


    @Override
    public void postDeleteCallback ( Asset a_deleted, AssetManager m_asset
                                     ) throws BaseException, AssetException, 
    GeneralSecurityException, RemoteException
    {
        postCreateCallback ( a_deleted, m_asset );
    }

}

