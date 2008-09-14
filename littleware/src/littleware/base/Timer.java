package littleware.base;

import java.util.Date;


/**
 * Little timer class
 */
public class Timer {
  /** Time timer started */
  private Date  t_start;

  /**
   * Start the timer at t_now
   */
  public Timer () {
    t_start = new Date ();
  }

  /**
   * Reset the timer so that t_start=t_now
   */
  public void reset () { t_start = new Date (); }

  /** 
   * Sample the number of milliseconds t_now-t_start
   */
  public long sample () {
    Date t_now = new Date ();
    return (t_now.getTime () - t_start.getTime ());
  }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

