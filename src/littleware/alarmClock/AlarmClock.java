package littleware.alarmClock;

import java.awt.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Little alarm clock class just tracks the
 * time till alarm should sound.
 * Works like an alarm clock - only looks at the 
 * hour, minute - and assumes 24 hour clock.
 */
public class AlarmClock extends Observable {
    private int     oi_hours = 0;
    private int     oi_minutes = 0;
    private boolean ob_on = false;
    private Logger  ox_logger = Logger.getLogger ( "littleware.alarmClock" );
    
    /**
    * Internal thread launched when the alarm gets turned on - 
    * counts down to alarm time, and sounds the alarm.
    */
    protected class AlarmThread implements Runnable, Observer {
        private Date    ot_last_check;
        private boolean ob_shutdown = false;
        
        /** Shutdown this thread - turnAlarmOff should call this */
        public synchronized void shutdown () {
            if ( false == ob_shutdown ) {
                ob_shutdown = true;
                notifyAll ();
            }
        }
        
        /** AlarmClock notifies us via this method whenever some data has changed */
        public synchronized void update ( Observable x_clock, Object x_arg ) {
            notifyAll ();
        }
        
        /** 
        * Wait until time to sound the next alarm, or until shutdown.
        * Sound the alarm for up to 2 minutes
        */
        public synchronized void handleAlarm () {
            addObserver( this );
            for ( long l_next_alarm_delta = getNextAlarmDate ().getTimeInMillis () - (new Date()).getTime ();
                  (! ob_shutdown) && (l_next_alarm_delta > 0);
                  l_next_alarm_delta = getNextAlarmDate ().getTimeInMillis () - (new Date()).getTime () ) { 

                // loop this, because may be woken up if somebody resets a turned-on alarm
                try {
                    wait ( l_next_alarm_delta );
                } catch ( InterruptedException e ) {
                }
            }
            
            // Sound the alarm for up to 2 minutes
            for ( int i = 0; (i < 120) && (! ob_shutdown); ++i ) {
                soundAlarm ();
                try {
                    wait( 1000 );
                } catch ( InterruptedException e ) {}
            }
            deleteObserver( this );
        }
        
        /** Runnable interface run method - just calls handleAlarm */
        public void run () {
            handleAlarm ();
        }
    }
    
    AlarmThread    ox_alarm_thread = null;
    
    /**
    * Default constructor just initializes the alarm to midnight
    */
    public AlarmClock () {
        super();
    }
     
    /**
    * Constructor sets the alarm to the specified time
    *
    * @param i_hours in 24 hour clock
    * @param i_minutes for 24 hour clock
    * @exception AlarmClockException if invalid parameters given
    */
    public AlarmClock ( int i_hours, int i_minutes ) throws AlarmClockException {
        super();
        setAlarm ( i_hours, i_minutes );
    }
    
    /**
     * Compute the next time the alarm will sound if the alarm is turned on
     */
     public Calendar getNextAlarmDate () {
         Calendar     t_now = Calendar.getInstance ();
         Calendar     t_next_alarm = Calendar.getInstance ();
         
         int          i_delta_minutes = (oi_minutes + 60 - t_now.get( Calendar.MINUTE )) % 60;
         int          i_delta_hours   = (oi_hours + 24 - t_now.get( Calendar.HOUR_OF_DAY )) % 24;

         t_next_alarm.roll ( Calendar.MINUTE, i_delta_minutes );
         t_next_alarm.add ( Calendar.HOUR_OF_DAY, i_delta_hours );
         
         // minute roll may push us into past ...
         if ( (0 == i_delta_hours) && (t_next_alarm.before ( t_now )) ) {
             t_next_alarm.add ( Calendar.DATE, 1 );
         }
         return t_next_alarm;
    }

      
    /**
    * Set the alarm time to i_hours:i_minutes 24 hour clock time
    *
    * @param i_hours for 24 hour clock
    * @param i_minutes
    * @exception AlarmClockException if invalid parameters given
    */
    public void setAlarm ( int i_hours, int i_minutes ) throws AlarmClockException {
        if ( (i_hours < 0) || (i_hours > 23) ) {
            throw new AlarmClockException ( "Hours must be >= 0, < 24: " + i_hours );
        }
        if ( (i_minutes < 0) || (i_minutes > 59) ) {
            throw new AlarmClockException ( "Minutes must be >= 0, < 60: " + i_minutes );
        }
        oi_hours = i_hours;
        oi_minutes = i_minutes;
        { // May need to wake up alarm thread if running
            ox_logger.log ( Level.INFO, "Alarm reset to : " + oi_hours + ":" + oi_minutes +
                            " -> " + getNextAlarmDate ().getTime () + 
                            ", current time is: " + new Date () + ", notifying observers ..." );
            setChanged ();
            notifyObservers ();
        }
    }
        
    /** Get the hour the alarm is set to - 24 hour clock */
    public int getAlarmHour () { return oi_hours; }
    /** Get the minute the alarm is set to */
    public int getAlarmMinute () { return oi_minutes; }
    
    /**
    * Launch a thread into the background that will soundAlarm
    * the next time the alarm's time comes around.
    * The AlarmThread stops sounding the alarm after 2 minutes.
    */
    public synchronized void turnAlarmOn () {
        if ( null == ox_alarm_thread ) {
            ox_logger.log ( Level.INFO, "activating alarm thread to sound at: " + getNextAlarmDate ().getTime () );
            ox_alarm_thread = new AlarmThread ();
            new Thread( ox_alarm_thread ).start ();
        }
    }
    
    /**
    * Turn the alarm off - disengage the soundAlarm thread
    */
    public synchronized void turnAlarmOff () {
        AlarmThread x_thread = ox_alarm_thread;
        if ( null != x_thread ) {
            x_thread.shutdown ();
            ox_alarm_thread = null;
        }
    }

    
    /**
    * Sound the alarm once - just makes a single beep.
    */
    public void soundAlarm () {
        Toolkit.getDefaultToolkit().beep();     
    }
    
    /**
    * Return true if the alarm is on, false otherwise
    */
    public boolean isAlarmOn () {
        return (null != ox_alarm_thread);
    }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

