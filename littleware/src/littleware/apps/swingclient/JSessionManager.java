package littleware.apps.swingclient;

import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.security.GeneralSecurityException;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.util.UUID;
import java.net.URL;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.apps.client.*;
import littleware.asset.AssetException;
import littleware.base.BaseException;
import littleware.base.UUIDFactory;
import littleware.base.AssertionFailedException;
import littleware.security.auth.SessionUtil;
import littleware.security.auth.SessionManager;
import littleware.security.auth.SessionHelper;


/** 
 * Swing view onto a SessionManager interface implementation -
 * just wraps a SessionManager implementation.
 * Implements the LoginTool interface by which an application
 * controller can listen for state-change events,
 * and get the SessionHelper associated with a successful
 * login, or deal with the results of a login failure.
 * This widget notifies each LittleListener of the results of
 * UI triggered calls to SessionManager#login or SessionManagaer#getSessionHelper
 * by sending each listener a {@link littleware.apps.client.LittleEvent} with
 * a &quot;login&quot; operation and a SessionHelper result on success.
 */
public class JSessionManager extends JPanel implements LittleTool, SessionManager {
	private static Logger olog_generic = Logger.getLogger ( "littleware.apps.swingclient.JSessionManager" );
	
	/**
	 * Setup the GUI to allow login via different methods 
	 */
	public enum LoginMethod { 
		/**
		 * User specifies name and password for login
		 */
		NAME_PASSWORD, 
		/** User specifies session-uuid for login */
		SESSION_UUID 
	}
	
	private SimpleLittleTool    otool_handler = new SimpleLittleTool ( this );
	private SessionManager      om_session = null;
	private LoginMethod         on_login = LoginMethod.NAME_PASSWORD;
	
	private JPanel              owp_name_password = new JPanel ( new GridBagLayout () );
	private JPanel              owp_session_uuid = new JPanel ( new FlowLayout () );
	private JTextField          ow_name = new JTextField ( 20 );
	private JPasswordField      ow_password = new JPasswordField ( 20 );
	private JTextField          ow_session_uuid = new JTextField ( 32 );
	private JButton             owb_login = null;
	
	{
		GridBagConstraints  grid_control = new GridBagConstraints ();
		grid_control.gridx = 0;
		grid_control.gridy = 0;
		
		owp_name_password.add ( new JLabel ( "Name: " ), grid_control );
		grid_control.gridx = GridBagConstraints.RELATIVE;
		grid_control.gridwidth = GridBagConstraints.REMAINDER;
		owp_name_password.add ( ow_name, grid_control );
		
		grid_control.gridx = 0;
		grid_control.gridy = 1;
		grid_control.gridwidth = 1;
		owp_name_password.add ( new JLabel ( "Password: " ), grid_control );
		grid_control.gridx = GridBagConstraints.RELATIVE;
		grid_control.gridwidth = GridBagConstraints.REMAINDER;
		grid_control.gridy = 1;
		owp_name_password.add ( ow_password, grid_control );
	}
	{
		owp_session_uuid.add ( new JLabel ( "Session UUID: " ) );
		owp_session_uuid.add ( ow_session_uuid );
	}
	
	/**
	 * Little internal utility to toggle display based
	 * on the active login mode
	 */
	private void toggleDisplayMode () {
		if ( on_login.equals ( LoginMethod.NAME_PASSWORD ) ) {
			owp_name_password.setVisible ( true );
			owp_session_uuid.setVisible ( false );
		} else {
			owp_name_password.setVisible( false );
			owp_session_uuid.setVisible( true );
		}
	}
	
	/**
	 * Intialize the GUI
	 */
	public void init ( boolean b_login_button ) {
		JPanel    wp_controls = new JPanel ();
		{
			/**
			 * Setup a combox box to change widget mode
			 */
			wp_controls.setBorder( BorderFactory.createEmptyBorder(30, 30, 10, 30) );
			wp_controls.setLayout( new FlowLayout ( FlowLayout.RIGHT ) );
			
			JComboBox w_mode = new JComboBox( LoginMethod.values () );
			w_mode.setSelectedIndex( on_login.ordinal () );
			w_mode.addActionListener(
									  new ActionListener () {
										  /** Listens to the combo box. */
										  public void actionPerformed(ActionEvent e) {
											  JComboBox   w_mode = (JComboBox) e.getSource ();
											  LoginMethod n_login = (LoginMethod) w_mode.getSelectedItem();
											  on_login = n_login;
											  toggleDisplayMode ();
											}
									  }										 
									  );
			wp_controls.add ( w_mode );
			toggleDisplayMode ();  // initialize mode correctly
		}		
			
		this.setLayout ( new GridBagLayout () );
		
        //Add Components to this panel.
        GridBagConstraints grid_control = new GridBagConstraints();
        grid_control.gridwidth = GridBagConstraints.REMAINDER;
		grid_control.gridx = 0;
		grid_control.gridy = 0;
		
		this.add ( wp_controls, grid_control );
		
        grid_control.gridy = 1;
        this.add( owp_name_password, grid_control );		
		grid_control.gridy = 2;
		this.add( owp_session_uuid, grid_control );
		grid_control.gridy = 3;
		
		if ( b_login_button ) {
			JPanel  wp_button_panel = new JPanel ();
			/** Pull label from resource-bundle later */
			owb_login = new JButton( "Login" );
			owb_login.addActionListener ( 
										 new ActionListener () {
											 public void actionPerformed(ActionEvent event_action ) {
												 try {
													 JSessionManager.this.getUiSessionHelper ();
												 } catch ( Exception e ) {
													 olog_generic.log ( Level.FINE, "Ignoring exception, because already propagated as a LittleEvent: " + e );
												 }
											 }
										 }
										 );
			wp_button_panel.add ( owb_login, BorderLayout.EAST );			
			this.add ( wp_button_panel, grid_control );
		}			
	}
	
	/**
	 * Constructor pulls internal SessionManager from SessionUtil.getSessionManager()
	 */
	public JSessionManager () throws RemoteException, NotBoundException {
		om_session = SessionUtil.getSessionManager ();
		init ( true );
	}
	
	/** 
	 * Constructor takes SessionManager implementation for the GUI to call through to
	 */
	public JSessionManager ( SessionManager m_wrapme ) {
		om_session = m_wrapme;
		init ( true );
	}
	
	/**
	 * Constructor allows widget setup withouth a login button
	 *
	 * @param m_wrapme SessionManager to wrap
	 * @param n_login specifes the initial login-method to setup the GUI for
	 * @param b_login_button set true (the default) to include a LOGIN button 
	 *             on the widget by which the user can trigger a login event,
	 *             false to not display the button - an external source must
	 *             trigger login.
	 */
	public JSessionManager ( SessionManager m_wrapme,
							 LoginMethod n_login,
							 boolean b_login_button ) {
		om_session = m_wrapme;
		on_login = n_login;
		init ( b_login_button );
	}
	
	
	public void	addLittleListener( LittleListener listen_little ) {
		otool_handler.addLittleListener ( listen_little );
	}
	
	
	public void     removeLittleListener( LittleListener listen_little ) {
		otool_handler.removeLittleListener ( listen_little );
	}
	
	/**
	 * Shared exception handler.
	 * Triggers a LittleEvent with the given exception,
	 * and rethrows the exception in a type-safe way.
	 *
	 * @param e_handle exception thrown
	 * @param s_operation to label dispatched LittleEvent with
	 */
	private void handleException ( Exception e_handle, 
								   String s_operation ) throws BaseException, AssetException,
		GeneralSecurityException, RemoteException
	{
		try {
			otool_handler.fireLittleEvent ( new LittleEvent ( this, s_operation, e_handle ) );
			throw e_handle;
		} catch ( RuntimeException e ) {
			throw e;
		} catch ( BaseException e ) {
			throw e;
		} catch ( GeneralSecurityException e ) {
			throw e;
		} catch ( RemoteException e ) {
			throw e;
		} catch ( Exception e ) {
			throw new AssertionFailedException ( "Invalid exception: " + e, e );
		}
	}
	
	/**
	 * Login via the internal SessionManager, and notify listeners
	 * of the result with a LittleEvent.
	 */
	public SessionHelper  login ( String s_name,
								  String s_password,
								  String s_session_comment
								  ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{		
		try {
			SessionHelper m_helper = om_session.login ( s_name, s_password, s_session_comment );
			otool_handler.fireLittleEvent ( new LittleEvent ( this, "login", m_helper ) );
			return m_helper;
		} catch ( Exception e ) {
			handleException ( e, "login" );
			throw new AssertionFailedException ( "Should not have reached this point - handleException should throw an exception" );
		}
	}

	
	public SessionHelper getSessionHelper ( UUID u_session
											) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		try {
			SessionHelper m_helper = om_session.getSessionHelper ( u_session );
			otool_handler.fireLittleEvent ( new LittleEvent ( this, "getSessionHelper", m_helper ) );
			return m_helper;
		} catch ( Exception e ) {
			handleException ( e, "getSessionHelper" );
			throw new AssertionFailedException ( "Should not have reached this point - handleException should throw an exception" );
		}	
	}
	

	public URL getUrl () throws RemoteException
	{
		return om_session.getUrl ();
	}
	
	/**
	 * Get the login button (if any - may be null).
	 * Mostly intended to support making the button the
	 * root-pane default button.
	 */
	public JButton getLoginButton () {
		return owb_login;
	}
	
	/**
	 * Process the username/password or session-uuid present in the UI
	 * to get a SessionHelper via login() or getSessionHelper().
	 * The Login button invokes this method, and the method is public
	 * to support putting this widget (with b_nobutton set in the constructor)
	 * into a Dialog where processing
	 * should be triggered by the dialog button.
	 */
	public SessionHelper getUiSessionHelper () throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		if ( on_login.equals ( LoginMethod.NAME_PASSWORD ) ) {
			return login ( ow_name.getText (), new String ( ow_password.getPassword () ), "JSessionManager login" );
		} else {
			return getSessionHelper ( UUIDFactory.parseUUID ( ow_session_uuid.getText () ) );
		}
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

