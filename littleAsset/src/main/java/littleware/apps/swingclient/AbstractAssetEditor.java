/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.swingclient;

import littleware.asset.client.AssetRef;
import java.beans.PropertyChangeEvent;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;

import littleware.base.BaseException;
import littleware.asset.AssetException;
import littleware.asset.Asset;
import littleware.asset.AssetBuilder;
import littleware.asset.client.AssetManager;


/** 
 * Basic UI-independent implementation of AssetEditor interface methods.
 * Intended for delegation by Swing-based AssetEditor implementors.
 */
public abstract class AbstractAssetEditor extends AbstractAssetView implements AssetEditor {
    private AssetBuilder                  localBuilder = null;
    private Asset                         localAsset = null;
    private boolean                       isChanged = false;

    
    /**
     * Constructor takes the bean to use as the source of any thrown events
     */
    public AbstractAssetEditor ( Object x_sourcebean ) {
        super ( x_sourcebean );
    }
    
        
    @Override
    public Asset getLocalAsset () {
        if ( null == localAsset ) {
            localAsset = localBuilder.build();
        }
        return localAsset;
    }
    
    /**
     * Set the AssetRef being viewed, and reset the local asset.
     */
    @Override
    public void setAssetModel ( AssetRef model_edit ) {
        if ( null == model_edit ) {
            return;
        }
        localBuilder = model_edit.getRef ().copy ();
        localAsset = null;
        // Do this last - it notifies observers
        super.setAssetModel ( model_edit );
        setHasLocalChanges ( false );
    }
        
        
    @Override
    public void setHasLocalChanges ( boolean value ) {
        if ( value != isChanged ) {
            isChanged = value;
            firePropertyChange ( new PropertyChangeEvent( getSourceBean (), 
                                                               AssetEditor.Property.hasLocalChanges.toString (),
                                                               ! isChanged,
                                                               isChanged
                                                               ) 
                                      );
        }
        localAsset = null;
    }
        
    @Override
    public boolean getHasLocalChanges () {
        return isChanged;
    }
    
    @Override
    public AssetBuilder changeLocalAsset () {
        setHasLocalChanges ( true );
        return localBuilder;
    }
    
    @Override
    public void clearLocalChanges () {
        final Asset clean = getAssetModel ().getRef ();
        localBuilder = clean.copy ();
        localAsset = null;
        setHasLocalChanges ( false );
    }
        
    @Override
    public void saveLocalChanges ( AssetManager assetMgr, String message
                                       ) throws BaseException, AssetException, 
        RemoteException, GeneralSecurityException
    {
        getAssetModel ().syncAsset ( assetMgr.saveAsset( localBuilder.build(), message ) );
        localBuilder = getAssetModel().getRef().copy();
        localAsset = null;
        setHasLocalChanges ( false );
    }


}



