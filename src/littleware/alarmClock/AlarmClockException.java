package littleware.alarmClock;

/**
 * Base exception for alarmClock package.
 */
public class AlarmClockException extends Exception {

    /** Do-nothing constructor */
    public AlarmClockException () {
        super( "Exception in littleware.alarmClock" );
    }
    
    /** Exception with user-supplied message */
    public AlarmClockException ( String s_message ) {
        super ( s_message );
    }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

