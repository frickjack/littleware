package littleware.apps.swingclient;

import java.util.*;
import javax.swing.SwingUtilities;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoableEditSupport;
import java.beans.PropertyChangeEvent;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;

import littleware.base.AssertionFailedException;
import littleware.base.BaseException;
import littleware.asset.AssetException;
import littleware.asset.Asset;
import littleware.asset.AssetManager;
import littleware.asset.InvalidAssetTypeException;


/** 
 * Basic UI-independent implementation of AssetEditor interface methods.
 * Intended for delegation by Swing-based AssetEditor implementors.
 */
public abstract class AbstractAssetEditor extends AbstractAssetView implements AssetEditor {
    protected final UndoableEditSupport   oundo_handler = new UndoableEditSupport ( this );
    private Asset                         oa_local = null;
    private boolean                       ob_changed = false;

    
    /**
     * Constructor takes the bean to use as the source of any thrown events
     */
    public AbstractAssetEditor ( Object x_sourcebean ) {
        super ( x_sourcebean );
    }
    
        
    public Asset getLocalAsset () {
        return oa_local;
    }
    
    /**
     * Set the AssetModel being viewed, and reset the local asset.
     */
    @Override
    public void setAssetModel ( AssetModel model_edit ) {
        if ( null == model_edit ) {
            return;
        }
        Asset a_new = model_edit.getAsset ();
        oa_local = model_edit.getAsset ().clone ();
        try {
            oa_local.sync ( a_new );
        } catch ( InvalidAssetTypeException e ) {
            throw new AssertionFailedException ( "Should not fail sync after clone", e );
        }
        // Do this last - it notifies observers
        super.setAssetModel ( model_edit );
        setHasLocalChanges ( false );
    }
        
        
    public void setHasLocalChanges ( boolean b_changed ) {
        if ( b_changed != ob_changed ) {
            ob_changed = b_changed;
            firePropertyChange ( new PropertyChangeEvent( getSourceBean (), 
                                                               AssetEditor.Property.hasLocalChanges.toString (),
                                                               ! ob_changed,
                                                               ob_changed
                                                               ) 
                                      );
        }
    }
        
    public boolean getHasLocalChanges () {
        return ob_changed;
    }
    
    public Asset changeLocalAsset () {
        setHasLocalChanges ( true );
        return getLocalAsset ();
    }
    
    public void clearLocalChanges () {
        Asset a_clean = getAssetModel ().getAsset ();
        try {
            oa_local = a_clean.clone ();
            oa_local.sync ( a_clean );
        } catch ( InvalidAssetTypeException e ) {
            throw new AssertionFailedException ( "Should not have happened", e );
        }
        setHasLocalChanges ( false );
    }
        
    public void saveLocalChanges ( AssetManager m_asset, String s_message 
                                       ) throws BaseException, AssetException, 
        RemoteException, GeneralSecurityException
    {
        Asset a_saved = m_asset.saveAsset ( oa_local, s_message );
        getAssetModel ().syncAsset ( a_saved );
        
        oa_local = a_saved.clone ();
        oa_local.sync ( a_saved );
        setHasLocalChanges ( false );
    }


    public void	addUndoableEditListener( UndoableEditListener listen_edit ) {
        oundo_handler.addUndoableEditListener ( listen_edit );
    }

    public void     removeUndoableEditListener( UndoableEditListener listen_edit ) {
        oundo_handler.removeUndoableEditListener ( listen_edit );
    }

}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

