/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.base.stat;

import java.util.Date;

/**
 * Little timer class
 */
public class Timer {

    /** Time timer started */
    private Date t_start;

    /**
     * Start the timer at t_now
     */
    private Timer() {
        t_start = new Date();
    }

    /**
     * Reset the timer so that t_start=t_now
     */
    public void reset() {
        t_start = new Date();
    }

    /**
     * Sample the number of milliseconds t_now-t_start
     */
    public long sample() {
        Date t_now = new Date();
        return (t_now.getTime() - t_start.getTime());
    }

    /**
     * Sample / 1000
     */
    public int sampleSeconds() {
        return (int) (sample() / 1000);
    }

    public static Timer startTimer() { return new Timer(); }
}

