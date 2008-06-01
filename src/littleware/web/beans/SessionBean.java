package littleware.web.beans;

import java.security.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.rmi.RemoteException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.*;

import littleware.security.*;
import littleware.security.auth.*;
import littleware.base.*;
import littleware.asset.*;
import littleware.apps.addressbook.*;
import littleware.apps.swingclient.AssetModelLibrary;
import littleware.apps.swingclient.SimpleAssetModelLibrary;

/**
 * Little security-info session-tracker for jsf web-session.
 * This is just a databucket with get/set routines.
 * The LoginForm is responsible for setting the
 * authenticatedUser property.
 * An unauthenticated session may still be granted
 * access to the repo by setting up a guest user session
 * for the session-bean to bootstrap to. 
 */
public class SessionBean {
    private static final Logger olog_generic = Logger.getLogger( SessionBean.class.getName() );
    private static SessionManager om_session = null;
    private static SessionHelper om_guest = null;
    private static boolean ob_initialized = false;


    /**
     * Little utility to setup the session connections to the asset server
     */
    private static synchronized void setupSession() {
        if (!ob_initialized) {
            try {
                if (null == om_session) {
                    om_session = BeanUtil.getSessionManager();
                }
                if (null == om_guest) {
                    PrivilegedAction<SessionHelper> act_getguest =
                            new PrivilegedAction<SessionHelper>() {

                                public SessionHelper run() {
                                    return BeanUtil.getWebGuestHelper();
                                }
                            };

                    om_guest = (SessionHelper) AccessController.doPrivileged(act_getguest);
                }
                ob_initialized = true;
            } catch ( Throwable e ) {
                olog_generic.log( Level.SEVERE, "Failed to setup session, caught: " + e
                        + ", " + BaseException.getStackTrace( e ), e
                        );
                throw new AssertionFailedException( "Caught throwable: " + e, e );
            }
        }
    }
    
    private SessionHelper om_helper = om_guest;
    private HttpSession ohttp_session = null;
    private AssetModelLibrary oalib_session = new SimpleAssetModelLibrary ();

    /**
     * Do-nothing constructor
     */
    public SessionBean() {
        if ((null == om_session) || (null == om_guest)) {
            setupSession();
            om_helper = om_guest;
        }
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
        AssetSearchManager m_search = om_helper.getService(ServiceType.ASSET_SEARCH);
        AssetManager m_asset = om_helper.getService(ServiceType.ASSET_MANAGER);
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
            a_link = m_search.getAsset(u_link);
        }

        Contact contact_user = null;

        if (null != a_link.getToId()) {
            contact_user = (Contact) m_search.getAssetOrNull(a_link.getToId());
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
            contact_user = (Contact) getAssetLib ().retrieveAssetModel( contact_user.getObjectId (), m_search).getAsset ();
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
        AssetSearchManager m_search = om_helper.getService(ServiceType.ASSET_SEARCH);
        
        return getContact( om_helper.getSession().getCreator(m_search) );
    }

    /** 
     * SessionHelper is available after successful authenticateAction
     */
    public SessionHelper getHelper() {
        return om_helper;
    }

    /**
     * Allow littleware.web.servlet.Security filter 
     * and loginForm to set the helper
     */
    public void setHelper(SessionHelper m_helper) throws RemoteException, AssetException,
            BaseException, GeneralSecurityException {
        om_helper = m_helper;
        AssetSearchManager m_search = m_helper.getService(ServiceType.ASSET_SEARCH);        

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
        return ((null == om_helper) || (om_helper == om_guest));
    }

    public LittleUser getUser () throws BaseException, RemoteException, GeneralSecurityException {
        return om_helper.getService( ServiceType.ACCOUNT_MANAGER ).getAuthenticatedUser();
    }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

