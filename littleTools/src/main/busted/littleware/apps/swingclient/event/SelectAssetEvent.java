/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.swingclient.event;

import littleware.asset.Asset;
import littleware.base.event.LittleEvent;

/**
 * Event triggered to indicate the user has selected some
 * single asset via some UI control.  Intended that
 * the purpose of the selection is implicit in whatever
 * control is the source.
 */
public class SelectAssetEvent extends LittleEvent {

    public static final String OS_OPERATION = "SelectAssetEvent";
    private final Asset selected;

    /**
     * Setup the SelectAssetEvent
     *
     * @param srouce of the event
     * @param selected Asset selected in UI
     */
    public SelectAssetEvent(Object srouce, Asset selected) {
        super(srouce);
        this.selected = selected;
    }

    /**
     * Get the Asset that has been selected
     */
    public Asset getSelectedAsset() {
        return selected;
    }
}
