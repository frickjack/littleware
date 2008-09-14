package littleware.base;

import java.security.*;

/**
 * Permission to access protected littleware resources.
 */
public class AccessPermission extends BasicPermission {
	/**
	* Constructor just passes permission spec to superclass
	 * as "littleware.db.resource." + s_name
	 *
	 * @param s_name of resource to access
	 */
	public AccessPermission ( String s_name ) {
		super ( "littleware.resource." + s_name );
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

