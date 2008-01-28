package littleware.web.beans;

import java.security.*;
import javax.security.auth.*;
import javax.security.auth.login.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.rmi.RemoteException;
import javax.mail.internet.*;
import javax.servlet.http.*;

import littleware.security.*;
import littleware.security.auth.*;
import littleware.base.*;
import littleware.asset.*;
import littleware.apps.addressbook.*;

/**
 * Little security-info session-tracker for jsf web-session.
 * This is just a databucket with get/set routines.
 * Unauthenticated user connects to littleware asset server as the user specified
 * by web.guest in littleware.properties.
 * Also includes methods to:
 *      authenticate/login user,
 *      change user password,
 *      get/set user contact info,
 *      check if a user has been authenticated for this session,
 *      get/set the SessionHelper for the logged in user session
 */
public class SessionBean {
	private static     Logger         olog_generic = Logger.getLogger ( "spijava.littleware.web.beans.SessionBean" );
	private static     SessionManager om_session = null;
	private static     SessionHelper  om_guest = null;
	private static     boolean        ob_initialized = false;
	
	static {
		setupSession ();
	}
	
	/**
	 * Little utility to setup the session connections to the asset server
	 */
	private static synchronized void setupSession () {	
		if ( ! ob_initialized ) {
			if ( null == om_session ) {
				om_session = BeanUtil.getSessionManager ();
			}
			if ( null == om_guest ) {
				PrivilegedAction<SessionHelper>  act_getguest = 
				new PrivilegedAction<SessionHelper> () {
					public SessionHelper run () {
						return BeanUtil.getWebGuestHelper ();
					}
				};
				
				om_guest = (SessionHelper) AccessController.doPrivileged ( act_getguest );
			}
			ob_initialized = true;
		}
	}
		
	

	private String         os_name = "guest";
	private String         os_authenticated = null;
	private String         os_password = null;
	private String         os_email = null;

	private String         os_error = null;
	private SessionHelper  om_helper = om_guest;
	private HttpSession    ohttp_session = null;
	

	/**
	 * Do-nothing constructor
	 */
	public SessionBean () {	
		if ( (null == om_session) || (null == om_guest) ) {
			setupSession ();
			om_helper = om_guest;
		}
	}
	
	public String getEnteredName () { return os_name; }
	public void setEnteredName ( String s_name ) { os_name = s_name; }
	
	/**
	 * Return the name of the last user that this session
	 * successfully authenticated as - null if never authenticated
	 */
	public String getAuthenticatedName () { return os_authenticated; }
	
	public void setPassword ( String s_password ) { os_password = s_password; }
	/** Always returns null */
	public String getPassword () { return null; }
	
	/**
	 * Used by the update-contact action.
	 */
	public void setEmail ( String s_email ) { os_email = s_email; }
	/**
	 * Initialized to the user contact e-mail by the authenticate action
	 */
	public String getEmail () {
		return os_email;
	}
	
	/**
	 * Let the freakin bean know which HTTP session it is associatd with
	 */
	public void setSession ( HttpSession http_session ) {
		ohttp_session = http_session;
	}
	
	/**
	 * Get the contact-information attached to the
	 * authenticated user.  Autogenerate info if not already present.
	 *
	 * @return Contact asset, or null if not yet authenticated
	 */
	public Contact getContact () throws RemoteException, 
		BaseException, GeneralSecurityException, AssetException 
	{
		if ( null == os_authenticated ) {
			return null;
		}
		AssetSearchManager m_search = om_helper.getService ( ServiceType.ASSET_SEARCH );
		AssetManager       m_asset = om_helper.getService ( ServiceType.ASSET_MANAGER );
		LittleUser         user_active = om_helper.getSession ().getCreator ( m_search );
		UUID               u_link = m_search.getAssetIdsFrom ( user_active.getObjectId (),
																  AssetType.LINK 
																  ).get ( "contact" );
		Asset              a_link = null;
		
		if ( null == u_link ) {
			a_link = AddressAssetType.LINK.create ();
			a_link.setName ( "contact" );
			a_link.setFromId ( user_active.getObjectId () );
			a_link.setHomeId ( user_active.getHomeId () );
            a_link.setOwnerId ( user_active.getObjectId () );
		} else {
			a_link = m_search.getAsset ( u_link );
		}
		
		Contact  contact_user = null;
		
		if ( null != a_link.getToId () ) {
			contact_user = (Contact) m_search.getAssetOrNull ( a_link.getToId () );
		}
		if ( null == contact_user ) {
			contact_user = AddressAssetType.CONTACT.create ();
			contact_user.setHomeId ( a_link.getHomeId () );
			contact_user.setLastName ( "unknown" );
			contact_user.setFirstName ( "unknown" );
            contact_user.setOwnerId ( user_active.getObjectId () );
			
			contact_user = (Contact) m_asset.saveAsset ( contact_user, "bootstrap user contact information" );
			a_link.setToId ( contact_user.getObjectId () );
			a_link = m_asset.saveAsset ( a_link, "Link to new contact information" );			
		}
		
		Address addr_first = contact_user.getFirstAddress ();
		if ( null == addr_first ) {
			Address  addr_bootstrap = AddressAssetType.ADDRESS.create ();
			addr_bootstrap.setHomeId ( contact_user.getHomeId () );
			addr_bootstrap.setName ( "default" );
			try {
				addr_bootstrap.setEmail ( new InternetAddress ( "unknown@unknown.com" ) );
			} catch ( AddressException e ) {
				olog_generic.log ( Level.INFO, "Failure setting bogus bootstrap e-mail address, caught: " + e );
			}
			addr_bootstrap.setAddressType ( AddressType.HOME );
			contact_user.addAddress ( addr_bootstrap, 0 );
			contact_user = (Contact) m_asset.saveAsset ( contact_user, "bootstrap user contact information" );
			addr_first = addr_bootstrap;
		} 
		
		return contact_user;
	}
			
	
	/** 
	 * SessionHelper is available after successful authenticateAction
	 */
	public SessionHelper getHelper () { return om_helper; }
    
    /**
     * Allow littleware.web.servlet.Security filter to set the helper
     */
    public void setHelper ( SessionHelper m_helper ) throws RemoteException, AssetException, 
        BaseException, GeneralSecurityException 
    { 
        om_helper = m_helper;
        AssetSearchManager m_search = m_helper.getService ( ServiceType.ASSET_SEARCH );
        os_authenticated = m_helper.getSession ().getCreator ( m_search ).getName ();
        
        Contact contact_user = getContact ();
        Address addr_first = contact_user.getFirstAddress ();
        os_email = addr_first.getEmail ().toString ();
    }        

	
	public String getError () { return os_error; }
	public void setError ( String s_error ) { os_error = s_error; }
		
	public enum Result { OK, FAILED, REMOTE_DOWN, LOGIN };
	
	/**
	 * Authenticate this object's user with this object's password,
	 * and assign this session an authenticated principal (accessible via getPrincipal).
	 *
	 * @exception GeneralSecurityException on failure to authenticate
	 */
	public String authenticateAction () {
		/*...
		if ( null == ohttp_session ) {
			setError ( "HttpSession not registered with SessionBean" );
			return Result.FAILED.toString ();
		}
		...*/
        os_error = null;

		try {            
            LoginContext x_login = new LoginContext ( "littleware.security.clientlogin",
													  new SimpleNamePasswordCallbackHandler ( getEnteredName (), os_password )
													  );
            x_login.login ();
            Subject j_login = x_login.getSubject ();
            Set<SessionHelper> v_creds = j_login.getPublicCredentials ( SessionHelper.class );
            
            if ( v_creds.isEmpty () ) {
                throw new FailedLoginException ( "No SessionHelper in public credentials after login" );
            }
            setHelper ( v_creds.iterator ().next () );
            
			return Result.OK.toString ();
		} catch ( RemoteException e ) {
			olog_generic.log ( Level.INFO, "Failed login for " + getEnteredName () + ", caught: " + e +
							   ", " + BaseException.getStackTrace ( e ) );

			return Result.REMOTE_DOWN.toString ();
		} catch ( Exception e ) {
			olog_generic.log ( Level.INFO, "Failed login for " + getEnteredName () + ", caught: " + e +
							   ", " + BaseException.getStackTrace ( e ) );
			setError ( "Failed login: " + e.toString ()  );
			return Result.FAILED.toString ();
		}
	}
	
	/**
	 * Let the user update his email as long as he is in
	 * the web-user group.
	 */
	public String updateContactAction () {		 
		try {
			if ( null == os_authenticated ) {
				setError ( "Must login" );
				return Result.LOGIN.toString ();
			}
			if ( null == os_email ) {
				setError ( "May not specify null e-mail address" );
				return Result.FAILED.toString ();
			}
			
			Address addr_first = getContact ().getFirstAddress ();
			addr_first.setEmail ( new InternetAddress ( os_email ) );
			olog_generic.log ( Level.FINE, "Pre-save address asset with email " + 
							   addr_first.getEmail ().toString () +
							   " and data: " + addr_first.getData () 
							   );
			
			AssetManager       m_asset = om_helper.getService ( ServiceType.ASSET_MANAGER );
			addr_first = (Address) m_asset.saveAsset ( addr_first, "update e-mail via web account update" ); 
			olog_generic.log ( Level.INFO, "Just saved address asset with email " + 
							   addr_first.getEmail ().toString () +
							   " and data: " + addr_first.getData () 
							   );
							
			os_error = null;
			return Result.OK.toString ();
		} catch ( RemoteException e ) {
			olog_generic.log ( Level.WARNING, "Caught: " + e + ", " +
							   BaseException.getStackTrace ( e )
							   );
			return Result.REMOTE_DOWN.toString ();
		} catch ( Exception e ) {
			olog_generic.log ( Level.INFO, "Failed contact update for " + getAuthenticatedName () + ", caught: " + e +
							   ", " + BaseException.getStackTrace ( e ) );
			setError ( "Failed update: " + e.toString ()  );
			return Result.FAILED.toString ();
		}
	}
	
	/**
	 * Let the user update his password as long as he is in
	 * the web-user group.
	 */
	public String updatePasswordAction () {		 
		try {
			if ( null == os_authenticated ) {
				setError ( "Must login" );
				return Result.LOGIN.toString ();
			}
			
			AccountManager  m_account = om_helper.getService ( ServiceType.ACCOUNT_MANAGER );
			m_account.updateUser ( (LittleUser) m_account.getPrincipal ( os_authenticated ), 
								   os_password, "Change password" 
								   );
			os_error = null;
			return Result.OK.toString ();
		} catch ( RemoteException e ) {
			olog_generic.log ( Level.WARNING, "Caught: " + e + ", " +
							   BaseException.getStackTrace ( e )
							   );
			return Result.REMOTE_DOWN.toString ();
		} catch ( Exception e ) {
			olog_generic.log ( Level.INFO, "Failed password update for " + getAuthenticatedName () + ", caught: " + e +
							   ", " + BaseException.getStackTrace ( e ) );
			setError ( "Failed update: " + e.toString ()  );
			return Result.FAILED.toString ();
		}
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

