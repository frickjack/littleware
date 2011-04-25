/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.asset.client.spi;

import com.google.inject.Singleton;
import littleware.base.event.LittleEventSource;

/**
 * Global event bus for remote-service events like 'AssetLoadedEvent'
 * and 'AssetDeletedEvent'.  Client-side service proxies should take
 * responsibility for firing the appropriate events onto the bus to
 * notify cache handlers and other client subsystems.
 */
@Singleton
public interface LittleServiceBus extends LittleEventSource {

    public void fireEvent( LittleServiceEvent ev );
}
