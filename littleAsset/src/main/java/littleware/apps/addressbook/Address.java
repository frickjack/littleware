package littleware.apps.addressbook;

import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import javax.mail.internet.InternetAddress;
import java.net.URL;

import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.base.UsaState;


/**
 * Interface exported by an Address asset.
 * Note that the getters may return null unless specified.
 */
public interface Address extends Asset {	
	/**
	 * Equivalent to getFromId ()
	 */
	public UUID getContactId ();
	
	/**
	 * Set the Contact to associate this address with.
	 * Equivalent to setFromId( addr_contact.getObjectId () )
	 */
	public void setContactId ( UUID u_contact );
		
	/**
	 * Home, Business, ...
	 */
	public AddressType getAddressType ();
	
	/**
	 * Get the user-supplied label assigned to
	 * this Address.  Default to getType().toString()
	 */
	public String getLabel ();
	
	/** 
	 * Get e-mail associated with this address,
	 * or null if unassigned
	 */
	public InternetAddress getEmail ();
	
	/**
	 * Get the URL associated with this address.
	 */
	public URL getUrl ();
	
	public String getPhone ();
	
	public String getCountry ();
	
	/** AOL instant messenger or iChat or whatever */
	public String getMessenger ();
	
	/** Snail mail */
	public String getSnailMail ();
	
	public UsaState getUsaState ();
	
	/** Zipcode */
	public String getZipcode ();
	
	public void setAddressType ( AddressType n_address );
	
	/**
	 * Override the label if getType().toString ()
	 * is not ok
	 */
	public void setLabel ( String s_label );
	
	/**
	 * Set the e-mail address - may be null
	 *
	 * @throws AddressException on illegally formatted e-mail address
	 */
	public void setEmail ( InternetAddress mail_address );
	
	public void setPhone ( String s_phone );
	
	public void setCountry ( String s_country );
	
	public void setUsaState ( UsaState n_state );

	public void setMessenger ( String s_messenger_spec );
	public void setSnailMail ( String s_mail );
	public void setZipcode ( String s_zip );
	public void setUrl ( URL url_user );
    
    public Address clone ();
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

