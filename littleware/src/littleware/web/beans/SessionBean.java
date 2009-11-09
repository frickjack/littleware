/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.web.beans;

import java.security.*;
import java.util.logging.Logger;
import java.rmi.RemoteException;
import javax.servlet.http.*;

import littleware.apps.client.*;
import littleware.security.*;
import littleware.security.auth.*;
import littleware.base.*;
import littleware.asset.*;

/**
 * Little security-info session-tracker for jsf web-session.
 * The session bean manages Guice injection to the
 * other beans in a user session via the visitor pattern.
 * To inject a JSF bean with Guice dependencies
 * simply annotate the bean class appropriately,
 * and JSF inject (via faces-config.xml) a SessionBean
 * property (setSessionBean/getSessionBean).
 */
public class SessionBean {
    private static final Logger log = Logger.getLogger( SessionBean.class.getName() );    
    private SessionHelper helper;
    private AssetModelLibrary oalib_session = new SimpleAssetModelLibrary ();

    /**
     * Do-nothing constructor
     */
    public SessionBean() {
    }
    

    /** 
     * SessionHelper is available after successful authenticateAction
     */
    public SessionHelper getHelper() {
        return helper;
    }

    /**
     * Allow littleware.web.servlet.Security filter 
     * and loginForm to set the helper
     */
    public void setHelper(SessionHelper m_helper) throws RemoteException, AssetException,
            BaseException, GeneralSecurityException {
        AssetSearchManager m_search = m_helper.getService(ServiceType.ASSET_SEARCH);        
        ouser = m_helper.getService( ServiceType.ACCOUNT_MANAGER ).getAuthenticatedUser();
        helper = m_helper;
    }
    
    /**
     * Property tracks a shared asset data-model library,
     * which also acts as a client-side asset cache.
     * 
     * @return the library property value
     */
    public AssetModelLibrary getAssetLib () {
        return oalib_session;
    }
    
    public void setAssetLib( AssetModelLibrary alib ) {
        oalib_session = alib;
    }

    /**
     * Property is true if the session
     * references a null user or is using the
     * reserved 'guest' user login.
     */
    public boolean isGuest () {
        return ((null == helper));// || (helper == om_guest));
    }

    private LittleUser ouser = null;
    
    /**
     * Get cached instance of the LittleUser asset to simplify access
     * to user name, etc.
     * 
     * @return LittleUser object set at time of authentication
     */
    public LittleUser getUser () {
        return ouser;
    }
}

