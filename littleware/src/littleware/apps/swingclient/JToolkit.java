package littleware.apps.swingclient;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import java.io.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.apps.client.*;
import littleware.base.*;
import littleware.base.swing.*;
import littleware.security.auth.*;


/**
 * Application controller.
 * Sets up wizard like app.
 * <ul>
 *   <li> authenticate via JSessionManager </li>
 *   <li> launch tools via JSessionHelper </li>
 * </ul>
 * Integrate with JTextAppender log output
 * and JScriptRunner object scripting.
 * Notifies listeners of UI state change attempts
 * via LittleEvent with new state as the event result.
 * Launch as app via main(), or setup in applet or
 * support frame via launchApp().
 */
public class JToolkit implements LittleTool {
	private final static Logger olog_generic = Logger.getLogger ( "littleware.apps.swingclient.JToolkit" );
	
	/**
	 * Wizard panel displays the active JSessionManager or JSessionHelper
	 * depending on the state.
	 */
	private   JPanel                  owp_wizard = new JPanel ();
	{
		owp_wizard.setLayout ( new BoxLayout ( owp_wizard, BoxLayout.Y_AXIS ) );
		owp_wizard.setBorder ( BorderFactory.createLoweredBevelBorder () );
	}
	private   JSessionManager         owm_session = null;
	private   JSessionHelper          owm_helper  = null;
	private   final JTextAppender     ow_appender = new JTextAppender ( 5, 60 );
	private   final JFrame            owframe_runner = new JFrame ( "ScriptRunner" );
	private   final JScriptRunner     ow_runner = new JScriptRunner ();	
	{
		// Setup a frame for the scriptrunner to popup in
		owframe_runner.add ( ow_runner );
		ow_runner.setBorder ( BorderFactory.createLoweredBevelBorder () );
		PrintWriter write_appender = new PrintWriter ( new BufferedWriter ( new AppenderWriter ( ow_appender ) ) );
		ow_runner.setWriter ( write_appender );
		ow_runner.setErrorWriter ( write_appender );
		ow_runner.registerBean ( "toolkit", this );
		ow_runner.append ( "// JToolkit registered as bean: toolkit\n" );
		owframe_runner.pack ();
		owframe_runner.setVisible( false );
		
		// add a button to the wizard panel to popup the script runner
		JPanel wp_script = new JPanel ( new FlowLayout () );
		wp_script.add ( new JLabel ( "Script runner: " ) );
		JButton wb_scriptlaunch = new JButton ( "Launch" );
		wb_scriptlaunch.addActionListener ( 
											new ActionListener () {
					public void actionPerformed(ActionEvent e) {
						setScriptRunnerVisible ( true );
					}
				}
											);
		wp_script.add ( wb_scriptlaunch );
		owp_wizard.add ( wp_script );
	}
	
	private  final SimpleLittleTool otool_handler = new SimpleLittleTool ( this );
	
	public void	addLittleListener( LittleListener listen_little ) {
		otool_handler.addLittleListener ( listen_little );
	}
	
	
	public void     removeLittleListener( LittleListener listen_little ) {
		otool_handler.removeLittleListener ( listen_little );
	}
	
    public void addPropertyChangeListener( PropertyChangeListener listen_props ) {
        otool_handler.addPropertyChangeListener ( listen_props );
    }
    
    public void removePropertyChangeListener( PropertyChangeListener listen_props ) {
        otool_handler.removePropertyChangeListener ( listen_props );
    }
    
	
	/**
	 * This controller follows a simple state diagram
	 */
	public enum State {
		/**
		 * LOGIN state - display SessionManager control,
		 * and allow user to login.  SessionHelper not yet available.
		 */
		LOGIN,
		/**
		 * A SessionHelper has been acquired via login or constructor,
		 * and the viewer exposes a JSessionHelper.
		 */
		TOOLBOX;
	}
	
	private State     on_state = State.LOGIN;
	
	/**
	 * JSessionManager listener active when in LOGIN state.
	 * Manages state transitions out of the LOGIN state.
	 */
	private LittleListener olisten_login = new LittleListener () {
		public void receiveLittleEvent ( LittleEvent event_little ) {
			if ( on_state.equals ( State.LOGIN ) ) {
				if ( event_little.isSuccessful () ) {
					owm_helper = new JSessionHelper ( (SessionHelper) event_little.getResult () );
					owp_wizard.add ( owm_helper );
					owm_session.setVisible ( false );
					owm_helper.setVisible ( true );					
					ow_runner.registerBean ( "helper", owm_helper );
					ow_runner.append ( "\n// SessionHelper bean registered with name: helper\n" );
					on_state = State.TOOLBOX;
					otool_handler.fireLittleEvent ( new LittleEvent ( JToolkit.this, "statechange",
																		 on_state )
													   );
					JButton wb_launch = owm_helper.getLaunchButton ();
					if ( null != wb_launch ) {
						wb_launch.getRootPane ().setDefaultButton ( wb_launch );
					}
				} else {
					// Need to come up with an internationalized error-message system
					olog_generic.log ( Level.INFO, "Failed login, caught: " + event_little.getResult () );
					ow_appender.append ( "Failed login, caught:  " + event_little.getResult () );
				}
			}
		}
	};
	
	
	/**
	 * Get a handle to the ScriptRunner managed by this tool.
	 */
	public JScriptRunner getScriptRunner () { return ow_runner; }
	/**
	 * Get the appender
	 */
	public JTextAppender getTextAppender () { return ow_appender; }
	/**
	 * Get the current state.
	 */
	public State getState () { return on_state; }
	/**
	 * Get the SessionHelper - null if still in LOGIN state.
	 */
	public JSessionHelper getSessionHelper () { return owm_helper; }
		
	/**
	 * Hide or show the ScriptRunner frame.
	 * Causes asynchronous event caller is not AWT dispatch thread.
	 */
	public void setScriptRunnerVisible ( final boolean b_visible ) {
		if ( SwingUtilities.isEventDispatchThread () ) {
			owframe_runner.setVisible ( b_visible );
		} else {
			SwingUtilities.invokeLater ( new Runnable () {
				public void run () {
					owframe_runner.setVisible ( b_visible );
				}
			}
										 );
		}
	}

	
	/** Do nothing hidden constructor */
	private JToolkit () {}
	
	/**
	 * Launch a toolkit controller in the given frame in LOGIN state.
	 *
	 * @param wframe_root in which to build our toolkit
	 * @return new JToolkit populating wframe_root
	 */
	public static JToolkit buildToolkit ( RootPaneContainer  wframe_root, SessionManager m_session ) {
		JToolkit   x_kit = new JToolkit ();
		Container w_content = wframe_root.getRootPane ().getContentPane ();
		w_content.setLayout ( new BoxLayout ( w_content, BoxLayout.Y_AXIS ) );
		x_kit.owm_session = new JSessionManager ( m_session );
		x_kit.owm_session.addLittleListener ( x_kit.olisten_login );
		x_kit.on_state = State.LOGIN;
		x_kit.owp_wizard.add ( x_kit.owm_session );
		w_content.add ( x_kit.owp_wizard );
		w_content.add ( x_kit.ow_appender );
		wframe_root.getRootPane ().setDefaultButton ( x_kit.owm_session.getLoginButton () );
		
		return x_kit;
	}
	
	/**
     * Launch a toolkit controller in the given frame in TOOOLBOX state.
	 *
	 * @param wframe_root in which to build our toolkit
	 * @param m_helper SessionHelper ready to be wrapped in a JSessionHelper
	 */
	public static JToolkit buildToolkit ( RootPaneContainer  wframe_root, SessionHelper m_helper ) {
		JToolkit   x_kit = new JToolkit ();
		Container  w_content = wframe_root.getRootPane ().getContentPane ();
		w_content.setLayout ( new BoxLayout ( w_content, BoxLayout.Y_AXIS ) );
		x_kit.owm_helper = new JSessionHelper ( m_helper );
		x_kit.on_state = State.TOOLBOX;
		x_kit.owp_wizard.add ( x_kit.owm_helper );
		w_content.add ( x_kit.owp_wizard );
		w_content.add ( x_kit.ow_appender );
		wframe_root.getRootPane ().setDefaultButton ( x_kit.owm_helper.getLaunchButton () );
		
		return x_kit;
	}

	/**
	 * Luanch a toolbox
	 */
	public static void main ( String[] v_argv ) {
		try {
			final JFrame w_root = new JFrame ();
			w_root.setDefaultCloseOperation ( JFrame.EXIT_ON_CLOSE );
			buildToolkit ( w_root, SessionUtil.get ().getSessionManager () );
			SwingUtilities.invokeLater (
									   new Runnable () {
										   public void run () {
											   w_root.pack ();
											   w_root.setVisible( true );
										   }
									   }
										);
		} catch ( RuntimeException e ) {
			throw e;
		} catch ( Exception e ) {
			throw new RuntimeException ( "Caught unexpected: " + e, e );
		}
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

