package littleware.security;

import java.util.ResourceBundle;
import java.security.*;

/**
 * Little PrivledgedAction implementation that attempts to
 * access the object behind the GuardedObject of the named
 * resource of the named ResourceBundle.
 */
public class GetGuardedResourceAction implements PrivilegedAction<Object> {
	private ResourceBundle ox_bundle;
	private String         os_resource;
	
	/**
	 * Constructor stashes theresource bundle, and the
	 * name of the resource that maps to a GuardedObject to access
	 */
	public GetGuardedResourceAction ( ResourceBundle x_bundle,
									  String s_resource ) {
		ox_bundle = x_bundle;
		os_resource = s_resource;
	}
	
	
	/**
	 * Constructor stashes the named resource bundle, and the
	 * name of the resource that maps to a GuardedObject to access
	 */
	public GetGuardedResourceAction ( String s_resource_bundle,
								String s_resource ) {
		ox_bundle = ResourceBundle.getBundle ( s_resource_bundle );
		os_resource = s_resource;
	}
	
	/**
	 * Pull that freakin' object out of the resource bundle
	 * and GuardedObject
	 *
	 * @return ConnectionFactory
	 */
	public Object run () {
		GuardedObject  x_guard = (GuardedObject) ox_bundle.getObject ( os_resource );
		return x_guard.getObject ();
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

