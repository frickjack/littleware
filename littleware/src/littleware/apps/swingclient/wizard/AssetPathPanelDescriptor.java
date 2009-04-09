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
    private final JAssetPathPanel   opanel_path;
    
    
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
    public AssetPathPanelDescriptor ( Object x_panel_id,
                                      JAssetPathPanel panel_path
                                ) {
        super ( x_panel_id, panel_path );
        opanel_path = panel_path;
    }
    
    @Override
    public void aboutToDisplayPanel () {
        UUID u_acl = getAssetId ();
        try {
            if ( null == u_acl ) {
                opanel_path.setAssetPath ( (AssetPath) null );
            } else {
                AssetPath path_acl = AssetPathFactory.getFactory ().createPath ( u_acl );
                opanel_path.setAssetPath ( path_acl );
            }
        } catch ( Exception e ) {
            JOptionPane.showMessageDialog( null,
                                           "Could not resolve AssetPath for: " + u_acl +
                                           ", caught: " + e, "alert", 
                                           JOptionPane.ERROR_MESSAGE
                                           );                                                   
        }
        
    }
    
    /** Set the focus */
    @Override
    public void displayingPanel () {
        opanel_path.requestFocus ();
    }
    
    @Override
    public void aboutToHidePanel () {
        try {
            opanel_path.setAssetPath ( opanel_path.getText () );
            UUID u_old = getAssetId ();
            UUID u_new = opanel_path.getAssetId ();
            if ( ! littleware.base.Whatever.equalsSafe ( u_old, u_new ) ) {
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

