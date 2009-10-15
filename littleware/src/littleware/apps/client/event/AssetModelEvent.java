/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.client.event;

import littleware.apps.client.LittleEvent;
import littleware.apps.client.AssetModel;

/**
 * Event triggered by a repository changes affecting assets in the AssetModelLibrary.
 * These events may be causally chained, so an Operation.assetDeleted
 * event may cause an Operation.assetsLinkingFrom event to fire.
 */
public class AssetModelEvent extends LittleEvent {

    private final AssetModel.Operation on_operation;
    private final AssetModelEvent oevent_cause;

    /**
     * Setup the AssetModelEvent
     *
     * @param model_source of the event
     * @param n_operation the user requested
     */
    public AssetModelEvent(AssetModel model_source, AssetModel.Operation n_operation) {
        this(model_source, n_operation, null, null);
    }

    /**
     * Setup the AssetModelEvent
     *
     * @param model_source of the event
     * @param n_operation the user requested
     * @param x_result result of operation - may be null     
     */
    public AssetModelEvent(AssetModel model_source, AssetModel.Operation n_operation,
            Object x_result) {
        this(model_source, n_operation, x_result, null);
    }

    /**
     * Setup the AssetModelEvent
     *
     * @param model_source of the event
     * @param n_operation the user requested
     * @param x_result result of operation - may be null
     * @param event_cause parent event that caused this one to fire - may be null
     */
    public AssetModelEvent(AssetModel model_source, AssetModel.Operation n_operation,
            Object x_result, AssetModelEvent event_cause) {
        super(model_source, n_operation.toString(), x_result);
        on_operation = n_operation;
        oevent_cause = event_cause;
    }

    /**
     * Get the operation enum - getOp () == getOperation ().toString ()
     */
    public AssetModel.Operation getOp() {
        return on_operation;
    }

    @Override
    public AssetModel getSource() {
        return (AssetModel) super.getSource();
    }

    /**
     * Get the event that caused this event to fire - may be null
     */
    public AssetModelEvent getCause() {
        return oevent_cause;
    }
}

