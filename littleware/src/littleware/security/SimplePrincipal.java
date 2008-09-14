package littleware.security;

import java.util.UUID;
import java.security.Principal;

import littleware.asset.*;

/**
 * Simple implementation of java.security.Principal 
 * interface backed by a database entry.
 * Protected instance - should only be created by
 * a LoginModule or a littleware.security.SecurityManager.
 * This class is specialized with an 'isAuthenticated' flag
 * so that clients may pass an instance through as a Principal
 * to a SecurityManager to verify whether a principal supplied
 * as a certificate is authenticated.
 */
public abstract class SimplePrincipal extends SimpleAsset implements LittlePrincipal {	
	/** Do-nothing constructor for java.io.Serializable */
	public SimplePrincipal () {}

    /** Covariant return-type clone */
    public SimplePrincipal clone () {
        return (SimplePrincipal) super.clone ();
    }
        

	/**
	 * Basic initializer just sets the principal name
	 *
	 * @param s_name must be alpha-numeric
	 * @param u_id littleware id number
	 * @param s_comment attached to user
	 */
	public SimplePrincipal ( String s_name, UUID u_id, String s_comment ) {
		this.setName ( s_name );
		this.setComment ( s_comment );
		this.setObjectId ( u_id );
	}
		

	
	/** Just return the name */
	public String toString () {
		return getName ();
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

