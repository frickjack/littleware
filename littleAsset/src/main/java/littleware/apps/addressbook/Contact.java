package littleware.apps.addressbook;

import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.List;

import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.base.BaseException;


/**
 * Interface exported by Contact asset
 * which associates an individual with a set of addresses.
 * Loading a Contact will load all the assets associated
 * with the Contact.
 * Note that AssetManager.save() will also result
 * in the save of all the Address assets associated
 * with this Contact - just to be safe that
 * everything is in sync.
 */
public interface Contact extends Asset {
	public String getFirstName ();
	public void setFirstName ( String s_first_name );
	
	public String getMiddleName ();
	public void setMiddleName ( String s_middle_name );
	
	/** Same as Asset.getName () */
	public String getLastName ();
	/** Same as Asset.setName () */
	public void setLastName ( String s_last_name );
	
	/**
	 * Get a copy of the ordered list of addresses associated
	 * with this Contact.  The order of the list
	 * is determined by the value() associated with
	 * each Address.
	 *
	 * @return unmodifiable list of addresses
	 */
	public List<Address>  getAddress ();
	
	/**
	 * Get the first address in the list of the given type.
	 * 
	 * @param n_type address-type to retrieve
	 * @return 1st address of type, or null if none available
	 */
	public Address getFirstAddress ( AddressType n_type );
	
	/**
	 * Get the first address in the address list, or null if list is empty
	 */
	public Address getFirstAddress ();
	
	/**
	 * Add the given address to the contact list.
	 * If another asset addr_old with the same object-id as
	 * addr_new is already in the list, then just remove addr_old
	 * before placing addr_new.
	 * Must AssetManager.saveAsset() this Contact to ensure
	 * maintenance of list order.
	 *
	 * @param addr_new to add or reorder - addr_new.setValue()
	 *              gets invoked to save its order,
	 *              and addr_new.setContactId () to save the Contact to
	 *              Address association, addr_new.setHomeId ( contact.getHomeId () ) too.
	 * @param i_position 0 for beginning, -1 for end
	 */
	public void addAddress ( Address addr_new, int i_position );
    
    /**
     * Clear all the addresses out of the internal list
     */
    public void clearAddress ();
	
	/**
	 * Remove the given address from the list, or do nothing if not there
	 */
	public void removeAddress ( Address addr_old );
    
    /** Covariant return-type clone */
    public Contact clone ();
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

