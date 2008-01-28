package littleware.apps.addressbook;

import java.util.Map;
import java.util.HashMap;


/**
 * Different types of addresses that might
 * be associated with a particular Contact
 */
public enum AddressType {
	HOME, BUSINESS, MOBILE, 
	/** preferred contact info of different types */
	PREFERRED, 
	OTHER;
	
	private static Map<String,AddressType>  ov_address = new HashMap<String,AddressType> ();
	
	static {
		for ( AddressType n_address : AddressType.values () ) {
			ov_address.put ( n_address.toString (), n_address );
		}
	}
	
	/**
	 * Return the AddressType with the given toString() representation,
	 * or null if no match
	 *
	 * @param s_name address-type to match
	 */
	public static AddressType parse ( String s_name ) {
		return ov_address.get ( s_name );
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

