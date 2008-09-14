package littleware.apps.addressbook;

import java.util.*;

import littleware.asset.*;

import littleware.base.UUIDFactory;
import littleware.base.FactoryException;
import littleware.security.auth.SimpleSession;
import littleware.security.auth.LittleSession;

/** 
 * AssetType specializer and bucket for littleware.apps.addressbook
 * based AssetTypes.
 */
public abstract class AddressAssetType<T extends Asset> extends AssetType<T> {
	private static AssetSpecializer   om_address = null;
	
	private static AssetSpecializer getAddressSpecializer () {
		if ( null == om_address ) {
			ResourceBundle bundle_address = ResourceBundle.getBundle ( "littleware.apps.addressbook.server.AddressResourceBundle" );
			om_address = (AssetSpecializer) bundle_address.getObject ( "littleware_address_specializer" );
		}
		return om_address;
	}
	
	
	public static final AssetType<Address> ADDRESS    = new AssetType<Address> (
																				UUIDFactory.parseUUID ( "D430F172C2F94F76ACDA39658027D95A" ),
																				"littleware.apps.addressbook.ADDRESS"
																				   ) {
		public AssetSpecializer getSpecializer () { return getAddressSpecializer (); }
		public Address create () throws FactoryException { return new SimpleAddress (); }
		public boolean mustBeAdminToCreate () { return false; }
	};

	public static final AssetType<Contact> CONTACT    = new AssetType<Contact> (
																				UUIDFactory.parseUUID ( "2EE7CCDE130D40A09184C2A3F88A6F25" ),
																				"littleware.apps.addressbook.CONTACT"
																				) {
		public AssetSpecializer getSpecializer () { return getAddressSpecializer (); }
		public Contact create () throws FactoryException { return new SimpleContact (); }
		public boolean mustBeAdminToCreate () { return false; }
	};	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

