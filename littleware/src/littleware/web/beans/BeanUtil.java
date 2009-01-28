/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.web.beans;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.security.*;
import javax.mail.*;
import java.rmi.RemoteException;

import littleware.base.*;
import littleware.security.auth.*;
import littleware.asset.*;

/**
 * Just a little place to stuff some shared data.
 * Should update this to load from Properties files later.
 */
public class BeanUtil {

    private static final Logger olog_generic = Logger.getLogger( BeanUtil.class.getName() );
    private static SessionManager om_session = null;
    // Need to be member of admin group to create new users
    private static SessionHelper om_admin = null;
    private static SessionHelper om_guest = null;
    private final static Properties oprop_mail = new Properties();
    private static String  os_webmaster_email = null;
    private static UUID    ou_webhome_id = null;
    private static boolean ob_initialized = false;


    static {
        setupSession();
    }


    /**
     * Get the home-id for the littleware webapp
     */
    public static UUID getWebHomeId () {
        return ou_webhome_id;
    }
    
    /**
     * Little utility to extend the timeout on a session 100 days.
     *
     * @param m_helper to extend session for
     */
    public static void extendSession100Days(SessionHelper m_helper)
            throws BaseException, RemoteException, GeneralSecurityException {
        // Now - extend the session so it does not expire for 100 days
        LittleSession a_session = m_helper.getSession();
        AssetManager m_asset = m_helper.getService(ServiceType.ASSET_MANAGER);
        Date t_end = new Date();
        t_end.setTime(t_end.getTime() + 100 * 24 * 60 * 60 * 1000L);
        a_session.setEndDate(t_end);
        a_session = (LittleSession) m_asset.saveAsset(a_session, "extend session 100 days");
    }

    /**
     * Little utility to setup the session connections to the asset server
     */
    private static synchronized void setupSession() {
        if (!ob_initialized) {
            try {
                Properties prop_littleware = PropertiesLoader.get ().loadProperties();

                os_webmaster_email = prop_littleware.getProperty("web.admin.email");
                if (null == os_webmaster_email) {
                    throw new AssertionFailedException("NULL web.admin e-mail from littleware.properties");
                }

                // Setup e-mail properties
                String[] v_props = {"mail.smtp.host", "mail.debug"};
                for (String s_name : v_props) {
                    String s_value = prop_littleware.getProperty(s_name);
                    if (null != s_value) {
                        oprop_mail.setProperty(s_name, s_value);
                    }
                }

                if (null == om_session) {
                    om_session = SessionUtil.get().getSessionManager();
                }
                if (null == om_admin) {
                    String s_user = prop_littleware.getProperty("web.admin");
                    String s_password = prop_littleware.getProperty("web.admin.password");

                    if ((null == s_user) || (null == s_password)) {
                        throw new AssertionFailedException("NULL web.admin config from littleware.properties (" +
                                s_user + ", " + s_password + ")");
                    }
                    om_admin = om_session.login(s_user, s_password, "admin login");
                    extendSession100Days(om_admin);
                }

                if (null == om_guest) {

                    String s_user = prop_littleware.getProperty("web.guest");
                    String s_password = prop_littleware.getProperty("web.guest.password");

                    if ((null == s_user) || (null == s_password)) {
                        olog_generic.log( Level.WARNING, "No guest user/password in littleware.properties - setting guest helper to null" );
                        //throw new AssertionFailedException("NULL web.guest config from littleware.properties (" +
                        //        s_user + ", " + s_password + ")");
                    } else {
                        om_guest = om_session.login(s_user, s_password, "reserved guest login");
                        extendSession100Days(om_guest);
                    }
                }
                if ( null == ou_webhome_id ) {
                    ou_webhome_id = om_admin.getService( ServiceType.ASSET_SEARCH ).getByName( "littleware.web_home", AssetType.HOME ).getObjectId ();
                }
                ob_initialized = true;
            } catch (RuntimeException e) {
                olog_generic.log( Level.SEVERE, "Failed to configure session, caught: " + e
                        + ", " + BaseException.getStackTrace( e )
                        );
                throw e;
            } catch (Throwable e) {
                olog_generic.log(Level.INFO, "Failed to configure SessionBean, caught: " + e +
                        ", " + BaseException.getStackTrace(e));
                throw new AssertionFailedException("Failed to configure SessionBean, caught: " + e, e);
            }
        }
    }
    
    /**
     * Get the webmaster e-mail address
     */
    public static String getWebMasterEmail() {
        return os_webmaster_email;
    }

    /**
     * Get the cached SessionManager
     */
    public static SessionManager getSessionManager() {
        return om_session;
    }

    /**
     * Get the web-admin authenticated SessionHelper.
     * Must have littleware.base.AccessPermission( "webadmin" ) -
     * otherwise SecurityException thrown.
     */
    public static SessionHelper getWebAdminHelper() {
        Permission perm_access = new AccessPermission("webadmin");
        AccessController.checkPermission(perm_access);
        return om_admin;
    }

    /**
     * Get the guest authenticated SessionHelper.
     * Must have littleware.base.AccessPermission( "webguest" ) -
     * otherwise SecurityException thrown.
     */
    public static SessionHelper getWebGuestHelper() {
        Permission perm_access = new AccessPermission("webguest");
        AccessController.checkPermission(perm_access);
        return om_guest;
    }

    /**
     * Create a javamail session setup to talk to the correct servers
     * based on the littleware.properties mail.* properties.
     * Does an Accesspermission security Permission check.
     *
     * @return server-initialized javax.mail Session
     */
    public static Session setupMailSession() {
        Properties prop_mail = new Properties(oprop_mail);
        return Session.getInstance(prop_mail, null);
    }
}

