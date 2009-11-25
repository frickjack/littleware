/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.swingclient.wizard;

import javax.swing.*;
import java.util.UUID;

import com.nexes.wizard.*;

import littleware.asset.AssetPath;
import littleware.asset.AssetPathFactory;


/** 
 * Abstract class for registering an AssetPathPanel with a wizard.
 * Subtypes should override getAssetId() and setAssetId ().
 * Useful for setting up getAclId/setAclId, getFromId/setFromId, whatever.
 * Subtypes should override [g/s]etAssetId, get[Back/Next]PanelDescriptor
 */
public abstract class AssetPathPanelDescriptor extends WizardPanelDescriptor {
    private final JAssetPathPanel   owPanelPath;
    private final AssetPathFactory  opathFactory;
    
    
    /**
     * Hook to get the id of the AssetPath to initialize
     * the AssetPathPanel to before displaying the panel.
     *
     * @return id to initialize AssetPath around - may be null
     */
    public abstract UUID getAssetId ();

    /**
     * Hook to set the id on the editor that this AssetPathPanel
     * is associated with before hiding the panel.
     *
     * @param u_path id derived from the current AssetPathPanel data - may be null
     */
    public abstract void setAssetId ( UUID u_path );
    
    /**
     * Constructor passes data through to super class
     */
    public AssetPathPanelDescriptor ( Object panelId,
                                      AssetPathFactory pathFactory,
                                      JAssetPathPanel wPanelPath
                                ) {
        super ( panelId, wPanelPath );
        owPanelPath = wPanelPath;
        opathFactory = pathFactory;
    }
    
    @Override
    public void aboutToDisplayPanel () {
        final UUID uId = getAssetId ();
        try {
            if ( null == uId ) {
                owPanelPath.setAssetPath ( (AssetPath) null );
            } else {
                final AssetPath path = opathFactory.createPath ( uId );
                owPanelPath.setAssetPath ( path );
            }
        } catch ( Exception e ) {
            JOptionPane.showMessageDialog( null,
                                           "Could not resolve AssetPath for: " + uId +
                                           ", caught: " + e, "alert", 
                                           JOptionPane.ERROR_MESSAGE
                                           );                                                   
        }
        
    }
    
    /** Set the focus */
    @Override
    public void displayingPanel () {
        owPanelPath.requestFocus ();
    }
    
    @Override
    public void aboutToHidePanel () {
        try {
            owPanelPath.setAssetPath ( owPanelPath.getText () );
            UUID u_old = getAssetId ();
            UUID u_new = owPanelPath.getAssetId ();
            if ( ! littleware.base.Whatever.get().equalsSafe ( u_old, u_new ) ) {
                setAssetId ( u_new );
            }
        } catch ( Exception e ) {
            JOptionPane.showMessageDialog(null, "Could not resolve path to ACL, caught: " + e,
                                          "alert", 
                                          JOptionPane.ERROR_MESSAGE
                                          );
        }
    }



}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

