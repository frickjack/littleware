package littleware.apps.swingclient;

import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.security.GeneralSecurityException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.util.UUID;
import java.util.Set;
import java.net.URL;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.asset.AssetException;
import littleware.base.BaseException;
import littleware.base.UUIDFactory;
import littleware.base.AssertionFailedException;
import littleware.base.NoSuchThingException;
import littleware.base.XmlSpecial;
import littleware.security.auth.SessionUtil;
import littleware.security.auth.SessionHelper;
import littleware.security.auth.ServiceType;
import littleware.security.auth.LittleSession;


/** 
 * Goofy little widget that lets the user launch different
 * littleware ServiceType controllers.
 * This widget notifies each LittleListener when the selected ServiceType
 * changes via a LittleEvent event with the service-type as a result.
 * This widget notifies listeners of the results of
 * buildServiceUI () calls 
 * by sending each listener a {@link littleware.apps.swingclient.LittleEvent} with
 * a &quot;launchServiceUI&quot; operation and the ServiceType result on success.
 * Need to come up with some mechanism to register new service UI later.
 */
public class JSessionHelper extends JPanel implements LittleTool, SessionHelper {
	private final static Logger        olog_generic = Logger.getLogger ( "littleware.apps.swingclient.JSessionHelper" );
	
	
	private final SimpleLittleTool    otool_handler = new SimpleLittleTool ( this );
	private SessionHelper             om_helper = null;
	
	private ServiceType               on_service = null;
	private JLabel                    ow_info = new JLabel ();
	private JButton                   owb_launch = null;
	
	/**
	 * Initialize the GUI with or without a launch button
	 *
	 * @param b_launch_button true to include a service-launch button,
	 *              otherwise it's up to the wrapping controller 
	 *              to do whatever it wants based on the current selection.
	 */
	private void init ( boolean b_launch_button ) {
		/**
		 * Setup a combox box to change widget mode
		 */
		this.setBorder( BorderFactory.createEmptyBorder(30, 30, 10, 30) );
		this.setLayout ( new GridBagLayout () );
	
		ServiceType[] v_service = ServiceType.getMembers ().toArray ( new ServiceType[0] );
		on_service = v_service[0];
		final JComboBox w_service = new JComboBox( v_service );
		w_service.setSelectedIndex( 0 );
		w_service.addActionListener(
								  new ActionListener () {
									  /** Listens to the combo box. */
									  public void actionPerformed(ActionEvent e) {
										  on_service = (ServiceType) w_service.getSelectedItem ();
										  otool_handler.fireLittleEvent ( new LittleEvent ( JSessionHelper.this,
																							   "selectServiceType",
																							   on_service
																							   )
																			 );
									  }
								  }										 
								  );
		
        //Add Components to this panel.
        GridBagConstraints grid_control = new GridBagConstraints();
        grid_control.gridwidth = GridBagConstraints.REMAINDER;
		grid_control.gridx = 0;
		grid_control.gridy = 0;
		
		this.add ( new JLabel ( "Welcome to Littleware!" ), grid_control );
        grid_control.gridy = GridBagConstraints.RELATIVE;
		this.add ( w_service, grid_control ); 
		grid_control.gridheight = 2;
		grid_control.fill = GridBagConstraints.HORIZONTAL;
		this.add ( ow_info, grid_control );
		grid_control.gridheight = 1;
		grid_control.fill = GridBagConstraints.NONE;
				   
		if ( b_launch_button ) {
			/** Pull label from resource-bundle later */
			owb_launch = new JButton( "Launch" );
			owb_launch.addActionListener ( 
										 new ActionListener () {
											 public void actionPerformed(ActionEvent event_action ) {
												 try {
													 JSessionHelper.this.buildServiceUI ( on_service );
													 ow_info.setText ( "" );
												 } catch ( Exception e ) {
													 olog_generic.log ( Level.FINE, "Ignoring exception, because already propagated as a LittleEvent: " + e );
													 ow_info.setText ( "<html><body><p><font color=\"red\">" +
																	   XmlSpecial.encode ( e.toString () ) +
																	   "</font></p></body></html>"
																	   );
												 }
											 }
										 }
										 );
			this.add ( owb_launch, grid_control );
		}			
	}
	

	
	/** 
	 * Constructor takes SessionHelper implementation for the GUI to call through to
	 */
	public JSessionHelper ( SessionHelper m_wrapme ) {
		om_helper = m_wrapme;
		init ( true );
	}
	
	/**
	 * Constructor allows widget setup withouth a launch button
	 *
	 * @param m_wrapme SessionHelper to bring up in LOGIN mode
	 * @param b_launch_button set true (the default) to include a launch button 
	 *             on the widget by which the user can trigger a call to launchServiceUI()
	 */
	public JSessionHelper ( SessionHelper m_wrapme,
							 boolean b_launch_button ) {
		om_helper = m_wrapme;
		init ( b_launch_button );
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
	
	public LittleSession getSession () throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		return om_helper.getSession ();
	}
	
	/**
	 * Just calls through to wrapped SessionHelper - no UI action taken.
	 */
	public <T extends Remote> T getService ( ServiceType<T> n_type ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		return om_helper.getService ( n_type );
	}
	
	/**
	 * Just calls through to wrapped SessionHelper - no UI action taken.
	 */
	public SessionHelper createNewSession ( String s_session_comment )
		throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		return om_helper.createNewSession ( s_session_comment );
	}
	
	
	/**
	 * Return a component UI for interfacing with the specified service.
	 * Pass that UI onto listeners via a LittleEvent with the UI as the event result.
	 *
	 * @param n_service to build a default UI around 
	 * @return default UI component interfacing with the result of getService( n_service )
	 * @exception NoSuchThingException if UI not yet available for requested service
	 */
	public JComponent buildServiceUI ( ServiceType n_service ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		BaseException e = new NoSuchThingException ( "UI not yet available for service: " + n_service );
		otool_handler.fireLittleEvent ( new LittleEvent ( this, "buildServiceUI", e ) );
		throw e;
	}
	
	/**
	 * Get the launch button (if any - may be null).
	 * Mostly intended to support making the button the
	 * root-pane default button.
	 */
	public JButton getLaunchButton () {
		return owb_launch;
	}
	
	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

