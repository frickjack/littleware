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
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.rmi.RemoteException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.*;

import littleware.apps.client.*;
import littleware.security.*;
import littleware.security.auth.*;
import littleware.base.*;
import littleware.asset.*;
import littleware.apps.addressbook.*;

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
    private HttpSession ohttp_session = null;
    private AssetModelLibrary oalib_session = new SimpleAssetModelLibrary ();

    /**
     * Do-nothing constructor
     */
    public SessionBean() {
    }
    

    /**
     * Let the freakin bean know which HTTP session it is associatd with
     */
    public void setSession(HttpSession http_session) {
        ohttp_session = http_session;
    }

    /**
     * Get the contact info for the user with the given id -
     * create and save stub info if Contact info does
     * not already exist for the given user.
     * 
     * @param user to get contact info for
     * @return Contact associated with user
     * @throws java.rmi.RemoteException
     * @throws littleware.base.BaseException
     * @throws java.security.GeneralSecurityException
     * @throws littleware.asset.AssetException
     */
    public Contact getContact( LittleUser user ) throws RemoteException,
            BaseException, GeneralSecurityException, AssetException {
        AssetSearchManager m_search = null; //helper.getService(ServiceType.ASSET_SEARCH);
        AssetManager m_asset = null; //helper.getService(ServiceType.ASSET_MANAGER);
        UUID u_link = m_search.getAssetIdsFrom(user.getObjectId(),
                AssetType.LINK).get("contact");
        Asset a_link = null;
        AssetBuilder builder = new AssetBuilder ().setHomeId( user.getHomeId () )
                .setOwnerId( user.getObjectId () );

        if (null == u_link) {
            a_link = builder.setName( "contact" )
                    .setFromId( user.getObjectId () )
                    .create ( AssetType.LINK );
        } else {
            a_link = m_search.getAsset(u_link).get();
        }

        Contact contact_user = null;

        if (null != a_link.getToId()) {
            contact_user = (Contact) m_search.getAsset(a_link.getToId()).getOr(null);
        }
        if (null == contact_user) {
            contact_user = builder.setFromId( null )
                    .setName( user.getName () )
                    .create ( AddressAssetType.CONTACT );
                        contact_user.setLastName("unknown");
            contact_user.setFirstName("unknown");
            a_link.setToId(contact_user.getObjectId());
        }

        Address addr_first = contact_user.getFirstAddress();
        if (null == addr_first) {
            Address addr_bootstrap = builder.setName( "default" )
                    .create( AddressAssetType.ADDRESS );
            try {
                addr_bootstrap.setEmail(new InternetAddress("unknown@unknown.com"));
            } catch (AddressException e) {
                throw new AssertionFailedException ( "Failure setting bogus bootstrap e-mail address", e );
            }
            addr_bootstrap.setAddressType(AddressType.HOME);
            contact_user.addAddress(addr_bootstrap, 0);
            
            List<Asset> v_save = new ArrayList<Asset> ();
            v_save.add ( contact_user );            
            v_save.add ( a_link );
            getAssetLib().syncAsset( m_asset.saveAssetsInOrder(v_save, "bootstrapping user contact" ) );
            // get the post-save asset
            contact_user = (Contact) getAssetLib ().retrieveAssetModel( contact_user.getObjectId (), m_search).get().getAsset ();
        }

        return contact_user;
    }
    
    /**
     * Get the contact-information attached to the
     * authenticated user.  Autogenerate info if not already present.
     *
     * @return Contact asset, or null if isGuest
     */
    public Contact getContact() throws RemoteException,
            BaseException, GeneralSecurityException, AssetException {
        if ( isGuest() ) {
            return null;
        }
        AssetSearchManager m_search = null; //helper.getService(ServiceType.ASSET_SEARCH);
        
        return getContact( helper.getSession().getCreator(m_search) );
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
        Contact contact_user = getContact();
        Address addr_first = contact_user.getFirstAddress();
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

