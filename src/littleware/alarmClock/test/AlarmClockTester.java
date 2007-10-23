package littleware.alarmClock.test;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import junit.framework.*;
import junit.swingui.TestRunner;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import littleware.alarmClock.*;

/**
 * Test fixture for AlarmClock class
 */
public class AlarmClockTester extends TestCase {

    private Logger  ox_logger = Logger.getLogger ( "littleware.alarmClock.test" );
    private AlarmClock ox_clock;
    
    /**
    * Constructor takes alarmclock instance to run test against
    *
    * @param s_name of test to run
    * @param x_clock to run test against
    */
    public AlarmClockTester ( String s_name, AlarmClock x_clock ) {
        super( s_name );
        ox_clock = x_clock;
    }
    
    /** No setup necessary */
    public void setUp () {}
    /** No teardown necessary */
    public void tearDown () {}
    
    /**
    * Just run a few generic tests
    */
    public void testGeneric () {
        try {
            ox_clock.setAlarm ( 23, 59 );
        } catch ( AlarmClockException e ) {
            assertTrue ( "Should not have caught exception setting alarm: " + e, false );
        }
        try {
            ox_clock.setAlarm ( 24, 59 );
            assertTrue ( "Should have thrown an exception - setting invalid alarm time 24:59", false );
        } catch ( AlarmClockException e ) {
        }
    
        ox_clock.soundAlarm ();
        int i_choice = JOptionPane.showOptionDialog( null,
                            "Just sounded alarm -\n  did you hear a beep ?",
                            "AlarmClockTester Query",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            null, null
                            );
        assertTrue( "Alarm did not sound", JOptionPane.YES_OPTION == i_choice );
        
        // Let's try to turn the alarm on and off
        ox_logger.log ( Level.INFO, "Setting alarm to t_now + 1 minute ..." );
        Calendar t_now_plus1 = Calendar.getInstance ();
        t_now_plus1.add ( Calendar.MINUTE, 1 );
        try {
            ox_clock.setAlarm ( t_now_plus1.get( Calendar.HOUR_OF_DAY ),
                                t_now_plus1.get( Calendar.MINUTE )
                                );
        } catch ( AlarmClockException e ) {
            assertTrue ( "Caught unexpected exception: " + e, false );
        }
        ox_clock.turnAlarmOn ();
        i_choice = JOptionPane.showOptionDialog( null,
                                                 "Alarm should sound in next 60 seconds.\n" +
                                                 " Press YES if alarm sounds repeatedly for up to 2 minutes.\n" +
                                                 " Press NO if test failed.",
                                                 "AlarmClockTester Query",
                                                 JOptionPane.YES_NO_OPTION,
                                                 JOptionPane.QUESTION_MESSAGE,
                                                 null,
                                                 null, null
                                                 );
        assertTrue( "turnAlarmOn failed", JOptionPane.YES_OPTION == i_choice );
        ox_clock.turnAlarmOff ();
    }
    
    /**
    * Setup a test suite to exercise this package
    */
    public static Test suite () {
        TestSuite x_suite = new TestSuite ();
        x_suite.addTest ( new AlarmClockTester( "testGeneric", new AlarmClock () ) );
        return x_suite;
    }
    
    /**
    * Launch the junit.swingui.TestRunner
    * For some reason the Xcode launcher does not like it if we ask it to call
    * junit.swingui.TestRunner.main() directly ?
    */
    public static void main ( String[] v_args ) {
        String[] v_launch_args = { "littleware.alarmClock.test.AlarmClockTester" };
        junit.swingui.TestRunner.main( v_launch_args );
    }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

