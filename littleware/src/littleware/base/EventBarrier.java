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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Simple event barrier.
 */
public class EventBarrier<T> implements Future<T> {
    private boolean  isEventReady = false;
    private T        data = null;

    /**
     * Wait for event data to be published
     *
     * @return the data once available
     * @throws java.lang.InterruptedException
     */
    public synchronized T  waitForEventData() throws InterruptedException {
        while ( ! isEventReady ) {
            wait();
        }
        return data;
    }

    /**
     * Publish event data
     *
     * @param data to supply to waiters
     * @exception IllegalStateException if event data already published to this barrier
     */
    public synchronized void publishEventData( T data ) {
        if ( isEventReady ) {
            throw new IllegalStateException( "Event data already published" );
        }
        this.data = data;
        isEventReady = true;
        notifyAll();
    }
    
    /** Non-blocking check to see if the event-data is ready */
    public boolean isDataReady () {
        return isEventReady;
    }

    @Override
    public String toString() {
        return "EventBarrier(isEventReady: " + isEventReady + ")";
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return this.isDataReady();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return this.waitForEventData();
    }

    @Override
    public synchronized T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if ( ! isEventReady ) {
            this.wait( TimeUnit.MILLISECONDS.convert(timeout, unit) );
        }
        if ( ! isEventReady ) {
            throw new TimeoutException();
        }
        return data;

    }

}
