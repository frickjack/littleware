package littleware.web.beans;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.security.*;
import javax.mail.*;
import java.rmi.RemoteException;

import littleware.base.*;
import littleware.security.auth.*;
import littleware.security.*;
import littleware.asset.*;

/**
 * Just a little place to stuff some shared data.
 * Should update this to load from Properties files later.
 */
public class BeanUtil {
	private static     Logger         olog_generic = Logger.getLogger ( "spijava.littleware.web.beans.BeanUtil" );	
	private static     SessionManager om_session = null;
	// Need to be member of admin group to create new users
	private static     SessionHelper  om_admin = null;
	private static     SessionHelper  om_guest = null;
    private final static Properties     oprop_mail = new Properties ();

	
	
	// web_home - should probably pull this stuff from a ResourceBundle ...
	private final static String       os_web_home = "littleware.web_home";
	private static     UUID           ou_web_home = null;
	// group of registered users
	private final static String        os_web_group = "group.littleware.web";
	// acl granting read-permission to the freakin' WEB_GROUP
	private final static String       os_web_acl_read = "acl.littleware.web.read";
	private static     UUID           ou_web_acl_read = null;
	private static     String         os_webmaster_email = null;

	private static     boolean        ob_initialized = false;

	static {
		setupSession ();
	}

    /**
     * Little utility to extend the timeout on a session 100 days.
     *
     * @param m_helper to extend session for
     */
    public static void extendSession100Days( SessionHelper m_helper ) 
            throws BaseException, RemoteException, GeneralSecurityException {
        // Now - extend the session so it does not expire for 100 days
        LittleSession a_session = m_helper.getSession ();
        AssetManager  m_asset = m_helper.getService ( ServiceType.ASSET_MANAGER );
        Date t_end = new Date ();
        t_end.setTime ( t_end.getTime () + 100*24*60*60*1000L );
        a_session.setEndDate ( t_end );
        a_session = (LittleSession) m_asset.saveAsset ( a_session, "extend session 100 days" );
    }    
	
	/**
	 * Little utility to setup the session connections to the asset server
	 */
	private static synchronized void setupSession () {	
		if ( ! ob_initialized ) {
			try {
				Properties      prop_littleware = PropertiesLoader.loadProperties ( "littleware.properties",
																					new Properties () 
																					);
				
				os_webmaster_email = prop_littleware.getProperty ( "web.admin.email" );
				if ( null == os_webmaster_email ) {
					throw new AssertionFailedException ( "NULL web.admin e-mail from littleware.properties" );
				}
                
                // Setup e-mail properties
                String[] v_props = { "mail.smtp.host", "mail.debug" };
                for ( String s_name : v_props ) {
                    String s_value = prop_littleware.getProperty ( s_name );
                    if ( null != s_value ) {
                        oprop_mail.setProperty ( s_name, s_value );
                    }
                }
								
				if ( null == om_session ) {
					om_session = SessionUtil.getSessionManager ();
				}
				if ( null == om_admin ) {
					String         s_user = prop_littleware.getProperty ( "web.admin" );
					String         s_password = prop_littleware.getProperty ( "web.admin.password" );
					
					if ( (null == s_user) || (null == s_password) ) {
						throw new AssertionFailedException ( "NULL web.admin config from littleware.properties (" +
														 s_user + ", " + s_password + ")"
														 );
					}
					om_admin = om_session.login ( s_user, s_password, "admin login" );
                    extendSession100Days ( om_admin );
				}
				
				if ( null == om_guest ) {
					
					String s_user = prop_littleware.getProperty ( "web.guest" );
					String s_password = prop_littleware.getProperty ( "web.guest.password" );
					
					if ( (null == s_user) || (null == s_password) ) {
						throw new AssertionFailedException ( "NULL web.guest config from littleware.properties (" +
														 s_user + ", " + s_password + ")"
														 );
					}
					om_guest = om_session.login ( s_user, s_password, "reserved guest login" );
                    extendSession100Days ( om_guest );
				}
				
				
				if ( null == ou_web_home ) {
					AssetSearchManager m_search = om_admin.getService ( ServiceType.ASSET_SEARCH );
					ou_web_home = m_search.getByName ( os_web_home,
																   AssetType.HOME
                                                       ).getObjectId ();
				}
				if ( null == ou_web_acl_read ) {
					AclManager m_acl = om_admin.getService ( ServiceType.ACL_MANAGER );
					
					LittleAcl acl_read = m_acl.getAcl ( os_web_acl_read );
					ou_web_acl_read = acl_read.getObjectId ();
				}
				
				ob_initialized = true;
            } catch ( RuntimeException e ) {
                throw e;
			} catch ( Exception e ) {
				olog_generic.log ( Level.INFO, "Failed to configure SessionBean, caught: " + e + 
								   ", " + BaseException.getStackTrace ( e )
								   );
				throw new AssertionFailedException ( "Failed to configure SessionBean, caught: " + e );
			}
		}
	}

	/**
	 * Get the name of the web-registered usr group
	 */
	public static  String getWebGroupName () { return os_web_group; }
	/**
	 * Get the webmaster e-mail address
	 */
	public static String getWebMasterEmail () { return os_webmaster_email; }
	/**
	 * Get the name of the HOME-asset for this webapp
	 */
	public static String getWebHomeName () { return os_web_home; }
	/**
	 * Get the id of the web home
	 */
	public static UUID getWebHomeId () { return ou_web_home; }
	/**
	 * Get the name of the read-only ACL for web-group members
	 */
	public static String getWebReadAclName () { return os_web_acl_read; }
	/**
	 * Get the id of the read-only ACL for web-group members
	 */
	public static UUID getWebReadAclId () { return ou_web_acl_read; }
	
	/**
	 * Get the cached SessionManager
	 */
	public static SessionManager getSessionManager () { return om_session; }
	/**
	 * Get the web-admin authenticated SessionHelper.
	 * Must have littleware.base.AccessPermission( "webadmin" ) -
	 * otherwise SecurityException thrown.
	 */
	public static SessionHelper getWebAdminHelper () {
		Permission perm_access = new AccessPermission ( "webadmin" );
		AccessController.checkPermission ( perm_access );
		return om_admin; 
	}
	/**
	 * Get the guest authenticated SessionHelper.
	 * Must have littleware.base.AccessPermission( "webguest" ) -
	 * otherwise SecurityException thrown.
	 */
	public static SessionHelper getWebGuestHelper () {
		Permission perm_access = new AccessPermission ( "webguest" );
		AccessController.checkPermission ( perm_access );
		return om_guest; 
	}	
	
    /**
     * Create a javamail session setup to talk to the correct servers
	 * based on the littleware.properties mail.* properties.
	 * Does an Accesspermission security Permission check.
	 *
	 * @return server-initialized javax.mail Session
	 */
	public static Session setupMailSession () {
		Properties     prop_mail = new Properties ( oprop_mail );
		return Session.getInstance ( prop_mail, null );
	}
        
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

