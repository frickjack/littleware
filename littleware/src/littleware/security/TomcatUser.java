package littleware.security;

import java.io.Serializable;
import java.security.Principal;

import littleware.security.auth.*;


/**
 * Stupid Principal implementation allows us to use
 * the Tomcat JAAS Realm to setup security, and still
 * get a handle to the SessionHelper established by
 * the littleware.security.auth.clientLoginModule.
 */
public class TomcatUser implements Serializable, Principal, Comparable<TomcatUser> {
    
    private final LittleUser    ouser_internal;
    private final SessionHelper om_helper;
    
    /**
     * Client stashes the LittleUser that this class wraps,
     * and the SessionHelper that handles the login session.
     */
    public TomcatUser( LittleUser user_internal, SessionHelper m_helper ) {
        ouser_internal = user_internal;
        om_helper = m_helper;
    }
    
    public String getName() {
        return ouser_internal.getName ();
    }
    
    /**
     * Get the SessionHelper assocaited with this Principal&apos;s login-session 
     */
    public SessionHelper getHelper () {
        return om_helper;
    }
    
    /**
     * Get the LittleUser this SessionHelper wraps
     */
    public LittleUser getLittleUser () {
        return ouser_internal;
    }
    
    public int hashCode() {
        return getName().hashCode();
    }
    
    public String toString() {
        return getName();
    }
    
    public int compareTo ( TomcatUser user_other ) {
        return getName ().compareTo ( user_other.getName () );
    }
    
    public boolean equals( Object x_other) {
        if ( ! (x_other instanceof TomcatUser) ) {
            return false;
        }
        final TomcatUser user_other = (TomcatUser) x_other;
        return user_other.getName().equals(getName());
    }
    
}    
