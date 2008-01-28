package littleware.security;

import java.util.UUID;
import java.security.*;

/**
 * Simple implementation of the SimpleUser interface
 */
public class SimpleUser extends SimplePrincipal implements LittleUser {
	/** Do-nothing constructor for java.io.Serializable */
	public SimpleUser () {
		setAssetType ( SecurityAssetType.USER );
	}
	
	public Status getStatus () {
		if ( getValue () == Status.ACTIVE.ordinal () ) {
			return Status.ACTIVE;
		}
		return Status.INACTIVE;
	}
	
	public void setStatus ( Status n_status ) {
		setValue ( n_status.ordinal () );
	}
	
	
	/**
	 * Basic initializer just sets the principal name
	 *
	 * @param s_name must be alpha-numeric
	 * @param u_id littleware id number
	 * @param s_comment attached to user
	 */
	public SimpleUser ( String s_name, UUID u_id, String s_comment ) {
		super( s_name, u_id, s_comment );
		setAssetType ( SecurityAssetType.USER );
	}
	
	/**
	 * Return a simple copy of this object
	 */
	public SimpleUser clone () {
		return (SimpleUser) super.clone ();
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

