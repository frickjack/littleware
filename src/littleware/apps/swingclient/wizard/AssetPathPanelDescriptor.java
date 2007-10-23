package littleware.apps.swingclient.wizard;

import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import javax.swing.*;
import javax.swing.event.*;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.nexes.wizard.*;

import littleware.asset.Asset;
import littleware.asset.AssetType;
import littleware.asset.AssetManager;
import littleware.asset.AssetPath;
import littleware.asset.AssetPathFactory;
import littleware.asset.AssetSearchManager;
import littleware.asset.AssetException;
import littleware.base.BaseException;
import littleware.apps.swingclient.*;


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
    public void displayingPanel () {
        opanel_path.requestFocus ();
    }
    
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

