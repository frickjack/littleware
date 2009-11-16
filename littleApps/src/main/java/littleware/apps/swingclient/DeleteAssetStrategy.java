/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.swingclient;

import com.google.inject.ImplementedBy;
import java.util.UUID;
import littleware.base.LittleBean;
import littleware.base.feedback.Feedback;

/**
 * Strategy for managing delete-asset user interaction.
 * Property change listeners can listen for when the
 * strategy changes state.
 */
public interface DeleteAssetStrategy extends LittleBean {
    /** Strategy processing state */
    public enum State {
        New, Scanning, CancelRequested, Canceled, Failed,
        Ready, Deleting, Success, Dismissed;
    };

    /**
     * Current strategy state engine state.
     * PropertyChangeEvent fired to listeners on state change.
     */
    public State getState();

    /**
     * Id of subtree root this strategy provides delete UI for
     */
    public UUID  getDeleteId();
    
    /**
     * May only be called once, otherwise throws IllegalStateException
     */
    public void launch();

    /**
     * Factory interface builds strategy with internal feedback
     * or client supplied feedback mechanism.
     * Returns strategy already advanced to Scanning state.
     * Swing implementations may require build on dispatch thread.
     */
    @ImplementedBy(JDeleteAssetBuilder.class)
    public interface Builder {
        public DeleteAssetStrategy build( UUID uIdToDelete );
        public DeleteAssetStrategy build( UUID uIdToDelete, Feedback feedback );
    }
}
