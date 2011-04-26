/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.swingclient.event;

import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.logging.Logger;
import java.util.logging.Level;


import littleware.apps.client.*;
import littleware.asset.Asset;
import littleware.asset.client.AssetManager;
import littleware.base.BaseException;
import littleware.base.event.LittleEvent;

/**
 * Event triggered to indicate a user request to save an edited
 * version of an Asset to the repository.
 * The doSave() method is the recommended way to save the data
 * if a controller decides to issue the save.
 */
public class SaveRequestEvent extends LittleEvent {

    private static final String OS_OPERATION = "SaveRequestEvent";
    private final AssetModelLibrary library;
    private final String updateComment;
    private final Asset changedAsset;

    /**
     * Setup the SaveRequestEvent
     *
     * @param source of the event
     * @param a_changed Asset to change - available via getChangedAsset()
     * @param lib_asset model library to update if the controller saves a_changed
     * @param s_update_comment to apply to the save
     */
    public SaveRequestEvent(Object source, Asset a_changed,
            AssetModelLibrary lib_asset,
            String s_update_comment) {
        super(source);
        this.library = lib_asset;
        this.updateComment = s_update_comment;
        this.changedAsset = a_changed;
    }

    /**
     * Get the Asset that has been requested to be saved
     */
    public Asset getChangedAsset() {
        return changedAsset;
    }

    /**
     * Get the update-comment to apply with the save.
     */
    public String getUpdateComment() {
        return updateComment;
    }

    /**
     * Get the AssetModelLibrary this app is working with
     */
    public AssetModelLibrary getModelLibrary() {
        return library;
    }

    /**
     * Recommended save method for controllers to ensure proper update of editors.
     * Subtypes may override for custom situations.
     * This implementation saves getChangedAsset() if its not null,
     * else getSource ().saveLocalChanges() if source is an AssetEditor,
     * else NOOP - just log a confused message.
     */
    public void doSave(AssetManager m_asset) throws BaseException, RemoteException,
            GeneralSecurityException {
        Asset a_save = getChangedAsset();
        Object x_source = getSource();

        if (null != a_save) {
            getModelLibrary().syncAsset(m_asset.saveAsset(a_save, getUpdateComment()));
        } else if (x_source instanceof AssetEditor) {
            ((AssetEditor) x_source).saveLocalChanges(m_asset, getUpdateComment());
        } else {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "NOOP on doSave()");
        }
    }
}
