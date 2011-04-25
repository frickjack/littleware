/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.client.spi;

import littleware.base.event.LittleEvent;

/**
 * LittleServiceEvent thrown on the Global LittleServiceBus event source
 */
public class LittleServiceEvent extends LittleEvent {
    private static final long serialVersionUID = 8882466619235817165L;

    public LittleServiceEvent( Object source ) {
        super( source );
    }
}
