/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.swingclient;

import littleware.asset.client.AssetSearchManager;
import littleware.asset.client.AssetManager;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.security.GeneralSecurityException;
import java.rmi.RemoteException;
import java.util.logging.Logger;

import com.nexes.wizard.*;

import littleware.asset.*;
import littleware.base.BaseException;
import littleware.apps.client.*;
import littleware.apps.swingclient.event.*;
import littleware.base.event.LittleEvent;
import littleware.base.event.LittleListener;
import littleware.base.feedback.Feedback;

/** 
 * Base class for a Wizard based AssetEditor.
 * Subtypes must implement propertyChange() to handle
 * updates to the underlying model.
 * Fires a littleware.apps.swingclient.event.SaveRequestEvent
 * on close() with Wizard.FINISH_RETURN_CODE.
 */
public abstract class WizardAssetEditor extends Wizard implements AssetEditor {
	private final static Logger           olog_generic = Logger.getLogger ( WizardAssetEditor.class.getName() );
    protected final AbstractAssetEditor   oeditor_internal = new AbstractAssetEditor ( this ) {
        @Override
        public void eventFromModel ( LittleEvent evt_model ) {
            WizardAssetEditor.this.eventFromModel ( evt_model );
        }
    };
    
    private final AssetSearchManager    om_search;
    private final AssetManager          om_asset;
    private final AssetModelLibrary     olib_asset;
    private final IconLibrary           olib_icon;
    
    /** Getter for subtypes to get at stashed tool */
    protected AssetSearchManager getSearchManager () { return om_search; }
    /** Getter for subtypes to get at stashed tool */
    protected AssetManager getAssetManager () { return om_asset; }
    
    /**
     * Constructor initalizes the underlying wizard and stashes
     * some managers for use by subtypes and panels.
     *
     * @param m_asset AssetManager to save changes with
     * @param m_search to lookup support data with
     * @param lib_icon source of icons
     */
    public WizardAssetEditor ( 
                                AssetManager m_asset,
                                AssetSearchManager m_search,
                                AssetModelLibrary lib_asset,
                                IconLibrary lib_icon ) 
    {   
        om_asset = m_asset;
        om_search = m_search;
        olib_icon = lib_icon;
        olib_asset = lib_asset;
	}
    

    /**
     * Constructor parents the Wizard to a Dialog owner
     */
    public WizardAssetEditor (
                               Dialog  wdialog_owner,
                               AssetManager m_asset,
                               AssetSearchManager m_search,
                              AssetModelLibrary lib_asset,
                               IconLibrary lib_icon ) 
    {
        super( wdialog_owner );
        om_asset = m_asset;
        om_search = m_search;
        olib_icon = lib_icon;
        olib_asset = lib_asset;        
    }
    
    @Override
    public AssetModel getAssetModel () {
        return oeditor_internal.getAssetModel ();
    }
    
    
    @Override
    public void setAssetModel ( AssetModel model_view ) {
        oeditor_internal.setAssetModel ( model_view );
    }
    
    
    
    @Override
    public Asset getLocalAsset () {
        return oeditor_internal.getLocalAsset ();
    }
    
    
    @Override
    public void setHasLocalChanges ( boolean b_changed ) {
        oeditor_internal.setHasLocalChanges ( b_changed );
    }
    
    @Override
    public boolean getHasLocalChanges () {
        return oeditor_internal.getHasLocalChanges ();
    }
    
    @Override
    public void saveLocalChanges ( AssetManager m_asset, String s_message 
                                   ) throws BaseException, AssetException, 
        RemoteException, GeneralSecurityException
    {
        oeditor_internal.saveLocalChanges ( m_asset, s_message );
    }
    
        
    @Override
    public void	addLittleListener( LittleListener listen_little ) {
		oeditor_internal.addLittleListener ( listen_little );
	}
	
	
    @Override
	public void     removeLittleListener( LittleListener listen_little ) {
		oeditor_internal.removeLittleListener ( listen_little );
	}
    
    @Override
    public AssetBuilder   changeLocalAsset () {
        return oeditor_internal.changeLocalAsset ();
    }
    
    @Override
    public void addPropertyChangeListener( PropertyChangeListener listen_props ) {
        oeditor_internal.addPropertyChangeListener ( listen_props );
    }
    
    @Override
    public void removePropertyChangeListener( PropertyChangeListener listen_props ) {
        oeditor_internal.removePropertyChangeListener ( listen_props );
    }
    
    
	
	public void fireLittleEvent ( LittleEvent event_little ) {
        oeditor_internal.fireLittleEvent ( event_little );
	}
    
    @Override
	public void clearLocalChanges () {
        oeditor_internal.clearLocalChanges ();
	}
    
    
    /**
     * Receive notification if our Model changes under us - update UI 
     * as necessary - refer to the {@link littleware.apps.swingclient.event.AssetEvent AssetEvent} subtypes
     * in the littleware.apps.swingclient.event package.
     * Subtypes should override and extend to update UI as necessary.
     */
	protected abstract void eventFromModel ( LittleEvent evt_model );
    
    /**
     * Fire a SaveRequestEvent if code is Wizard.FINISH_RETURN_CODE,
     * and call through to the super class
     */
    @Override
    protected void close(int i_code) {
        super.close( i_code );
        if ( Wizard.FINISH_RETURN_CODE == i_code ) {
            oeditor_internal.fireLittleEvent ( new SaveRequestEvent ( this, getLocalAsset (), 
                                                                      olib_asset, "creating new asset"
                                                                      ) 
                                               );
        }
    }

    @Override
    public Feedback getFeedback() {
        return oeditor_internal.getFeedback();
    }

    @Override
    public void setFeedback(Feedback feedback) {
        oeditor_internal.setFeedback( feedback );
    }


    
}    