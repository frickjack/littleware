/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.client;

import littleware.asset.client.AssetRef;
import littleware.base.event.LittleEvent;

/**
 * Event triggered by a repository changes affecting assets in the AssetModelLibrary.
 * These events may be causally chained, so an Operation.assetDeleted
 * event may cause an Operation.assetsLinkingFrom event to fire.
 */
public class AssetActionEvent extends LittleEvent {

    private final AssetRef.Operation operation;
    private final AssetActionEvent cause;


    /**
     * Setup the AssetActionEvent
     *
     * @param source of the event
     * @param operation the user requested
     */
    public AssetActionEvent(AssetRef source, AssetRef.Operation operation) {
        this(source, operation, null );
    }


    /**
     * Setup the AssetActionEvent
     *
     * @param source of the event
     * @param operation the user requested
     * @param cause parent event that caused this one to fire - may be null
     */
    public AssetActionEvent(AssetRef source, AssetRef.Operation operation,
            AssetActionEvent cause) {
        super(source );
        this.operation = operation;
        this.cause = cause;
    }

    /**
     * Get the operation enum - getOp () == getOperation ().toString ()
     */
    public AssetRef.Operation getOp() {
        return operation;
    }

    @Override
    public AssetRef getSource() {
        return (AssetRef) super.getSource();
    }

    /**
     * Get the event that caused this event to fire - may be null
     */
    public AssetActionEvent getCause() {
        return this.cause;
    }
}

