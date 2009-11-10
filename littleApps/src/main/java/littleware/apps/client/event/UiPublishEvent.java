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
import littleware.apps.client.Feedback;

/**
 * Fired to LittleListeners on Feedback.publish
 */
public class UiPublishEvent extends LittleEvent {
    private static final long serialVersionUID = -4235650543612558331L;

    public UiPublishEvent( Feedback source, Object x_result ) {
        super( source, "UiPublishEvent", x_result );
    }
}
