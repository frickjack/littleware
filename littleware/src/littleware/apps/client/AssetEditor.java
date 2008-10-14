package littleware.apps.client;

import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import javax.swing.*;
import javax.swing.event.*;

import littleware.asset.Asset;
import littleware.asset.AssetManager;
import littleware.asset.AssetException;
import littleware.base.BaseException;

/**
 * An AssetView specialization adds methods common
 * to many AssetEditor components.
 */
public interface AssetEditor extends AssetView {
    public enum Property {
        localAsset, hasLocalChanges
    }
    
    /**
     * Get the local-clone of the Asset from the AssetModel this editor
     * is observing.  The editor applies changes to this AssetModel,
     * and posts UndoableEditEvents to listeners which may implement
     * undo/redo ops.
     */
    public Asset getLocalAsset ();
    
    /**
     * Return true if the editor has made changes to its local asset
     * that have not been synced with the model library via
     * saveLocalchanges().
     */
    public boolean getHasLocalChanges ();
    
    /**
     * Set the result of hasLocalChanges () -
     * fire a PropertyChangeEvent.
     */
    public void setHasLocalChanges ( boolean b_value );
    
    /**
     * Resync the local asset with the underlying model -
     * discarding local changes - setHasLocalChanges( false ).
     */
    public void clearLocalChanges ();
    
    /**
     * Convenience function: setHashLocalChanges( true ); return getLocalAsset ();
     */
    public Asset changeLocalAsset ();
    
    /**
     * Convenience method.
     * Save whatever changes have been made so far if necessary,
     * and sync with the underlying model library, and setHasLocalChanges( false ).
     *
     * @param m_asset manager to save the asset with
     * @param s_message to apply with the update
     */
    public void saveLocalChanges ( AssetManager m_asset, String s_message 
                              ) throws BaseException, AssetException, 
        RemoteException, GeneralSecurityException;
    
    /**
     * Register a listener for UndoableEditEvents.
	 *
	 * @param listen_edit to add
     * @see javax.swing.UndoableEditSupport
	 */
	public void	addUndoableEditListener( UndoableEditListener listen_edit ) ;
	
	/**
     * Remove the given edit listener.
	 *
	 * @param listen_edit to remove
     * @see javax.swing.UndoableEditSupport
	 */
	public void     removeUndoableEditListener( UndoableEditListener listen_edit ) ;
    
}


// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

