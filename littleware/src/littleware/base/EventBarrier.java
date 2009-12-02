/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.base;

/**
 * Simple event barrier.
 */
public class EventBarrier<T> {
    private boolean  obEventReady = false;
    private T        odata = null;

    /**
     * Wait for event data to be published
     *
     * @return the data once available
     * @throws java.lang.InterruptedException
     */
    public synchronized T  waitForEventData() throws InterruptedException {
        while ( ! obEventReady ) {
            wait();
        }
        return odata;
    }

    /**
     * Publish event data
     *
     * @param data to supply to waiters
     * @exception IllegalStateException if event data already published to this barrier
     */
    public synchronized void publishEventData( T data ) {
        if ( obEventReady ) {
            throw new IllegalStateException( "Event data already published" );
        }
        odata = data;
        obEventReady = true;
        notifyAll();
    }
    
    /** Non-blocking check to see if the event-data is ready */
    public boolean isDataReady () {
        return obEventReady;
    }

}
