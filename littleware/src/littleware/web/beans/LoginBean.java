/*
 * Copyright (c) 2007,2008 Reuben Pasquini (catdogboy@yahoo.com)
 * All Rights Reserved
 */

package littleware.web.beans;

import java.rmi.RemoteException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.*;
import javax.security.auth.login.*;

import littleware.base.BaseException;
import littleware.security.auth.SimpleNamePasswordCallbackHandler;
import littleware.security.auth.SessionHelper;

/**
 * Manage login process.
 * Authenticate user, and register sesion
 * with SessionHelper bean. 
 */
public class LoginBean extends AbstractBean {
    private static final Logger olog_generic = Logger.getLogger( LoginBean.class.getName () );
    private String os_name = null;
    
    /**
     * Username property
     */
    public String getName() {
        return os_name;
    }

    public void setName(String s_name) {
        os_name = s_name;
    }


    private String os_password = null;
    
    /**
     * Password property
     * 
     * @param s_password
     */
    public void setPassword(String s_password) {
        os_password = s_password;
    }

    /** Always returns null */
    public String getPassword() {
        return null;
    }

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
        setLastResult( ActionResult.Ok );

        try {
            LoginContext x_login = new LoginContext("littleware.security.clientlogin",
                    new SimpleNamePasswordCallbackHandler(getName(), os_password)
                    );
            x_login.login();
            Subject j_login = x_login.getSubject();
            Set<SessionHelper> v_creds = j_login.getPublicCredentials(SessionHelper.class);

            if (v_creds.isEmpty()) {
                throw new FailedLoginException("No SessionHelper in public credentials after login");
            }
            getSessionBean ().setHelper(v_creds.iterator().next());            
        } catch (RemoteException e) {
            olog_generic.log(Level.INFO, "Failed login for " + getName() + ", caught: " + e +
                    ", " + BaseException.getStackTrace(e));

            setLastResult( ActionResult.Error );
        } catch (Exception e) {
            olog_generic.log(Level.INFO, "Failed login for " + getName() + ", caught: " + e +
                    ", " + BaseException.getStackTrace(e));
            setLastResult( ActionResult.Failed );
        } finally {
            return getLastResult().toString();
        }
    }

}
