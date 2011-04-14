/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.swingclient.event;

import littleware.base.feedback.LittleEvent;

/**
 * Event fired on user request to refresh view with fresh
 * data from the asset repository.
 */
public class RefreshRequestEvent extends LittleEvent {
    private static final String   OS_OPERATION = "DeleteRequestEvent";
    private static final long serialVersionUID = -3222503112892197737L;

    public RefreshRequestEvent( Object xSource ) {
        super( xSource, OS_OPERATION );
    }
}
