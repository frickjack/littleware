/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.base;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.rmi.RemoteException;

/**
 * Little utility class to fascilitate code reuse
 * between methods that want to reconnect and retry
 * when catching a RemoteException.
 * In general subtypes should override handle()
 * with something that invokes super.handle( e ),
 * then invokes situation specific retry logic
 * if an exception is not thrown by super.
 */
public class RemoteExceptionHandler {

    private static final Logger log = Logger.getLogger(RemoteExceptionHandler.class.getName());
    /**
     * Default number of times client may invoke
     * handle( RemoteException e ) before handle just starts throwing
     * the RemoteException.
     */
    private final static int DEFAULT_MAX_CALLS = 3;
    /**
     * Default number of seconds to sleep after incrementing
     * the retry count if the retry count does not exceed
     * the object maximum
     */
    private final static long DEFAULT_SLEEP_MS = 3000;

    private final int maxRetries;
    private final long retrySleepMs;
    private int counter = 0;

    /**
     * Do nothing constructor - sets up defaults values
     */
    public RemoteExceptionHandler() {
        this( DEFAULT_MAX_CALLS, DEFAULT_SLEEP_MS );
    }

    /**
     * Constructor allows override of max calls to handle,
     * and handle-sleep.
     *
     * @param l_sleep_ms no sleep if <= 0
     */
    public RemoteExceptionHandler(int i_max, long l_sleep_ms) {
        maxRetries = i_max;
        retrySleepMs = l_sleep_ms;
    }

    /**
     * Get count of handle() calls so far
     */
    public int getHandleCount() {
        return counter;
    }

    /** Get limit on handle calls before handle just throws its RemoteException argument */
    public int getHandleMax() {
        return maxRetries;
    }

    /** Get the ms handle() sleeps if it does not throw the RemoteException */
    public long getSleepMs() {
        return retrySleepMs;
    }

    /**
     * Handler increments count, throws RemoteException argument if
     * count exceeds max, otherwise logs the exception with a stack trace,
     * and sleeps for configured period.
     *
     * @param ex2handle  exception to throw if count exceeds max
     */
    public void handle(RemoteException ex2handle ) throws RemoteException {
        ++counter;
        if (counter > maxRetries) {
            throw ex2handle ;
        }
        log.log(Level.INFO, "RemoteException retry count " + counter, ex2handle  );
        if (retrySleepMs > 0) {
            try {
                Thread.sleep(retrySleepMs);
            } catch (InterruptedException ex ) {
                log.log(Level.INFO, "Sleep interrupted, caught: " + ex );
            }
        }
    }
}
