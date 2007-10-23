package littleware.alarmClock.swing;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.util.logging.*;

import littleware.alarmClock.*;

/**
 * Simple extention of JPanel that just puts an alarm clock inside.
 */
public class JAlarmClock extends JPanel {
    private AlarmClock  ox_clock;
    private JComboBox   ow_hours;
    private JComboBox   ow_minutes;
    private Logger      ox_logger = Logger.getLogger ( "littleware.alarmClock.swing" );
    
    /**
     * Constructor sets up the internal widgets ready 
     * to be packed up or set visible.
     * Caller needs to reset that if that is not
     * the behavior desired.
     */
    public JAlarmClock () {
        super();
        ox_clock = new AlarmClock ();
        
        String[] v_hours = new String[ 24 ];
        String[] v_minutes = new String[ 60 ];
        
        for ( int i = 0; i < 10; ++i ) {
            v_hours[ i ] = "0" + i;
            v_minutes[ i ] = v_hours[ i ];
        }
        for ( int i = 10; i < 24; ++i ) {
            v_hours[ i ] = Integer.toString ( i );
        }
        for ( int i = 10; i < 60; ++i ) {
            v_minutes[ i ] = Integer.toString ( i );
        }
        
        ow_hours = new JComboBox ( v_hours );
        ow_minutes = new JComboBox ( v_minutes );
        
        this.setLayout ( new GridLayout( 0, 1 ) );
        
        JLabel w_info = new JLabel ( "<html><b> Alarm Clock </b><br>" +
                                     "An alarm sounds for 2 minutes<br>" +
                                     "at the specified time." + 
                                     "</html>"
                                     );
        JPanel w_clock_pane = new JPanel ();
        w_clock_pane.add ( ow_hours );
        w_clock_pane.add ( ow_minutes );
        
        this.add ( w_clock_pane );
        this.add ( w_info );
        
        ActionListener x_listener = new ActionListener () {
            /** Implementation of ActionListener interface */
            public void actionPerformed( ActionEvent x_event ) {
                comboActionPerformed( x_event );
            }
        };
            
        ow_hours.addActionListener ( x_listener );
        ow_minutes.addActionListener ( x_listener );
        
        try {
            ox_clock.setAlarm ( 0, 0 );
            ox_clock.turnAlarmOn ();
        } catch ( AlarmClockException e ) {}
        
    }
    
    /** Implementation of ActionListener interface handles events on the combo boxes */
    protected void comboActionPerformed( ActionEvent x_event ) {
        if ("comboBoxChanged".equals( x_event.getActionCommand())) {
            try { // update the alarm clock
                ox_clock.setAlarm ( ow_hours.getSelectedIndex (),
                                    ow_minutes.getSelectedIndex()
                                    );  
            } catch ( AlarmClockException e ) {
                ox_logger.log ( Level.WARNING, "JAlarmClock caught unexpexpected: " + e );
            }
        }
    }
    
    
    /**
     * Receive Observer updates from wrapped alarm clock
     */
    public void update ( Observable x_clock, Object x_arg ) {
    }
    
    /**
     * Get the next time the alarm will sound if it is turned on
     */
    public Calendar getNextAlarmDate () { return ox_clock.getNextAlarmDate (); }
    
    /** Return true if the alarm is on */
    public boolean isAlarmOn () {
        return ox_clock.isAlarmOn ();
    }
    
    /**
     * Allow other parts of the program outside of this widget to
     * reset this alarm.
     */
    public void resetAlarm ( AlarmClock x_clock ) { 
        ox_clock.turnAlarmOff ();
        ox_clock = x_clock;
    }
    
    
    
    /**
     * Launch a stand-alone JAlarmClock
     */
    public static void main ( String[] v_args ) {
        Logger.getLogger ( "littelware.alarmClock" ).setLevel ( Level.INFO );
        JFrame  w_root = new JFrame ( "Alarm Clock" );
        
        w_root.getContentPane ().add ( new JAlarmClock () );
        w_root.setDefaultCloseOperation ( JFrame.EXIT_ON_CLOSE );
        w_root.pack ();
        w_root.setVisible ( true );
    }

}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

