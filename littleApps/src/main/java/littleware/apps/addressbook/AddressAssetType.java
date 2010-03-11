package littleware.apps.addressbook;


import littleware.asset.*;

import littleware.base.UUIDFactory;
import littleware.base.FactoryException;

/** 
 * AssetType specializer and bucket for littleware.apps.addressbook
 * based AssetTypes.
 */
public abstract class AddressAssetType<T extends Asset> extends AssetType<T> {
	
	public static final AssetType<Address> ADDRESS    = new AssetType<Address> (
																				UUIDFactory.parseUUID ( "D430F172C2F94F76ACDA39658027D95A" ),
																				"littleware.apps.addressbook.ADDRESS"
																				   ) {

		public Address create () throws FactoryException { return new SimpleAddress (); }
        @Override
		public boolean mustBeAdminToCreate () { return false; }
	};

	public static final AssetType<Contact> CONTACT    = new AssetType<Contact> (
																				UUIDFactory.parseUUID ( "2EE7CCDE130D40A09184C2A3F88A6F25" ),
																				"littleware.apps.addressbook.CONTACT"
																				) {
		public Contact create () throws FactoryException { return new SimpleContact (); }
        @Override
		public boolean mustBeAdminToCreate () { return false; }
	};	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

