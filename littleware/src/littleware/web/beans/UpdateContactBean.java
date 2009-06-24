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

import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.internet.*;

import littleware.apps.addressbook.*;
import littleware.asset.AssetManager;
import littleware.asset.AssetSearchManager;
import littleware.base.BaseException;
import littleware.base.UsaState;
import littleware.security.LittleUser;
import littleware.security.SecurityAssetType;
import littleware.security.auth.ServiceType;
import littleware.security.auth.SessionHelper;

/**
 * Bean backing contact-update form.
 */
public class UpdateContactBean extends AbstractBean {
    private static final Logger olog = Logger.getLogger( UpdateContactBean.class.getName () );
    private String os_name = null;
    private String os_email = null;
    private UsaState on_state = null;
    private String os_city = null;

    public String getName() {
        return os_name;
    }

    private String  os_first = null;
    
    /**
     * Property for user's real first name
     */
    public String getFirstName() {
        return os_first;
    }
    public void setFirstName( String s_first ) {
        os_first = s_first;
    }
    
    private String os_last;
    
    /**
     * Property for user's last name.
     */
    public String getLastName() {
        return os_last;
    }
    
    public void setLastName ( String s_last ) {
        os_last = s_last;
    }
    
    public String getEmail() {
        return os_email;
    }

    public void setEmail(String s_email) {
        os_email = s_email;
    }

    public String getCity() {
        return os_city;
    }

    public void setCity(String s_city) {
        os_city = s_city;
    }

    public UsaState getUsaState() {
        return on_state;
    }

    public void setUsaState(UsaState n_state) {
        on_state = n_state;
    }

    /**
     * Little utility to help subtypes -
     * just transfers this bean's properties to the
     * given Address, but does not save the address
     * to the repository.
     * 
     * @param addr to transfer properties to
     */
    protected void applyUpdatesToAddress ( Address addr ) 
            throws BaseException, AddressException
    {
        addr.setEmail(new InternetAddress(os_email));
        olog.log(Level.FINE, "Pre-save address asset with email " +
                addr.getEmail().toString() +
                " and data: " + addr.getData());        
    }
    

    private String os_user = null;
    
    /**
     * Username property.
     * Allow administrators to change password for any user
     */
    public String getUser() {
        return os_user;
    }
    public void setUser( String s_user ) {
        // set this bean's properties based on the assigned user
        try {
            AssetSearchManager search = getSessionBean ().getHelper().getService( ServiceType.ASSET_SEARCH );
            LittleUser user = search.getByName ( s_user, SecurityAssetType.USER ).get();
            Address addr = getSessionBean ().getContact ( user ).getFirstAddress ();
            
            os_user = s_user;
            setEmail( addr.getEmail().toString() );
        } catch ( RuntimeException e ) {
            throw e;
        } catch ( Exception e ) {
            olog.log( Level.WARNING, "Failure resolving address info for: " + s_user 
                    + ", caught: " + e + ", " + BaseException.getStackTrace( e ) 
                    );
        }
    }

    @Override
    public void setSessionBean ( SessionBean bean ) {
        super.setSessionBean( bean );
        try {
            setUser( bean.getUser ().getName () );
        } catch ( Exception e ) {
            olog.log( Level.WARNING, "Caught unexpected querying session user: " + e
                    + BaseException.getStackTrace( e ) 
                    );
        }
    }

    
    /**
     * Let the user update his email as long as he is in
     * the web-user group.
     */
    public String updateContactAction() {
        setLastResult ( ActionResult.Ok );
        try {
            if ( getSessionBean ().isGuest () ) {
                setLastResult ( ActionResult.MustLogin );
                return getLastResult ().toString ();
            }
            if (null == os_email) {
                setLastResult( ActionResult.Failed );
                return getLastResult ().toString ();
            }

            Address addr = null;
            
            if ( null == os_user ) {
                addr = getSessionBean ().getContact().getFirstAddress();
            } else {
                AssetSearchManager search = getSessionBean ().getHelper().getService( ServiceType.ASSET_SEARCH );
                LittleUser user = search.getByName ( os_user, SecurityAssetType.USER ).get();
                addr = getSessionBean ().getContact ( user ).getFirstAddress ();
            }
            applyUpdatesToAddress( addr );
            AssetManager m_asset = getSessionBean ().getHelper ().getService(ServiceType.ASSET_MANAGER);
            addr = (Address) m_asset.saveAsset(addr, "update e-mail via web account update");
            olog.log(Level.INFO, "Just saved address asset with email " +
                    addr.getEmail().toString() +
                    " and data: " + addr.getData());
            
        } catch (RemoteException e) {
            olog.log(Level.WARNING, "Caught: " + e + ", " +
                    BaseException.getStackTrace(e));
            setLastResult( ActionResult.Error );
        } catch (Exception e) {
            olog.log(Level.INFO, "Failed contact update for " 
                    + getSessionBean ().getHelper ().getSession ().getOwnerId () 
                    + ", caught: " + e 
                    + ", " + BaseException.getStackTrace(e)
                    );
            setLastResult( ActionResult.Failed );            
        } finally {
            return getLastResult ().toString();
        }
    }
    
}
