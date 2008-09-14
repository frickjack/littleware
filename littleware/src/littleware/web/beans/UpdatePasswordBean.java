/*
 * Copyright (c) 2007,2008 Reuben Pasquini
 * All Rights Reserved
 */

package littleware.web.beans;

import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.base.BaseException;
import littleware.security.AccountManager;
import littleware.security.LittleUser;
import littleware.security.auth.ServiceType;

/**
 * JSF form for verifying password
 */
public class UpdatePasswordBean extends AbstractBean {
    private static final Logger olog_generic = Logger.getLogger( UpdatePasswordBean.class.getName());
    private String  os_user = null;
    
    /**
     * Username property.
     * Allow administrators to change password for any user
     */
    public String getUser() {
        return os_user;
    }
    public void setUser( String s_user ) {
        os_user = s_user;
    }
    
    @Override
    public void setSessionBean ( SessionBean bean ) {
        super.setSessionBean( bean );
        try {
            os_user = bean.getUser ().getName ();
        } catch ( Exception e ) {
            olog_generic.log( Level.WARNING, "Caught unexpected querying session user: " + e
                    + BaseException.getStackTrace( e ) 
                    );
        }
    }
    
    private String  os_password1 = null;
    
    /**
     * New password property
     * @return empty-string - don't give out password
     */
    public String getPassword1() {
        return "";
    }
    public void setPassword1( String s_password ) {
        os_password1 = s_password;
    }
    
    private String os_password2 = null;
    
    /**
     * Verify-password property
     * @return
     */
    public String getPassword2() {
        return "";
    }
    public void setPassword2( String s_password ) {
        os_password2 = s_password;
    }
    
    /**
     * Let the user update his password as long as he is in
     * the web-user group.
     */
    public String updatePasswordAction() {
        if ( getSessionBean ().isGuest() ) {
            setLastResult( ActionResult.MustLogin );
            return getLastResult().toString();
        }
        if ( ! os_password1.equals( os_password2 ) ) {
            setLastResult( ActionResult.PasswordMismatch );
            return getLastResult().toString();
        }
        if ( os_password1.length () < 6 ) {
            setLastResult( ActionResult.BadPassword );
            return getLastResult().toString ();
        }
        String s_user = getUser();
        try {            
            AccountManager m_account = getSessionBean ().getHelper().getService(ServiceType.ACCOUNT_MANAGER);
            LittleUser user = (null == s_user) ? m_account.getAuthenticatedUser()
                    : (LittleUser) m_account.getPrincipal(s_user);
            s_user = user.getName();
            os_user = s_user;
            m_account.updateUser( user,
                    os_password1, "Change password");
            setLastResult( ActionResult.Ok );
        } catch (RemoteException e) {
            olog_generic.log(Level.WARNING, "Caught: " + e + ", " +
                    BaseException.getStackTrace(e));
            setLastResult( ActionResult.Failed );            
        } catch (Exception e) {
            olog_generic.log(Level.INFO, "Failed password update for " + s_user + ", caught: " + e +
                    ", " + BaseException.getStackTrace(e));
            setLastResult( ActionResult.Failed );
        }
        return getLastResult().toString();
    }

}
