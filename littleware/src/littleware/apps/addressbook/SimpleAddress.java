package littleware.apps.addressbook;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import java.io.*;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import javax.mail.internet.InternetAddress;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.net.URL;
import java.net.MalformedURLException;

import littleware.base.UsaState;
import littleware.asset.*;
import littleware.asset.xml.*;
import littleware.base.BaseException;
import littleware.base.XmlSpecial;
import littleware.base.ParseException;


/**
 * Simple implementation of Address interface
 */
public class SimpleAddress extends SimpleXmlDataAsset implements Address {	
	private static Logger  olog_generic = Logger.getLogger ( "littleware.apps.addressbook.SimpleAddress" );
	
	private AddressType          on_address = AddressType.OTHER;
	private String               os_phone = null;
	private InternetAddress      omail_address = null;
	private String               os_address = null;
	private String               os_messenger = null;
	private String               os_label = null;
	private URL                  ourl_address = null;
	private String               os_country = null;
	private UsaState             on_state = null;
	private String               os_zip = null;
	private String               os_mail = null;

	public final static String  OS_XML_NAMESPACE = SimpleContact.OS_XML_NAMESPACE;
	public final static String  OS_XML_PREFIX = SimpleContact.OS_XML_PREFIX;
	public final static String  OS_XML_ROOT = "addr_info";
	
	
	/**
	 * Constructor registers asset-type and XML support-data
	 * with super.
	 */
	public SimpleAddress () {
		super ( OS_XML_NAMESPACE, OS_XML_PREFIX, OS_XML_ROOT );
		setAssetType ( AddressAssetType.ADDRESS );
		setName ( UUID.randomUUID ().toString () );
	}
	
	public UUID getContactId () {
		return getFromId ();
	}
	
	public void setContactId ( UUID u_contact ) {
		setFromId ( u_contact );
	}
	
	@XmlGetter( element="", attribute="address_type" )
	public AddressType getAddressType () {
		return on_address;
	}
	
    @XmlGetter( element="label" )
	public String getLabel () {
		if ( null == os_label ) {
			return on_address.toString ();
		}
		return os_label;
	}
	
	@XmlGetter( element="email" )
	public InternetAddress getEmail ()
	{
		return omail_address;
	}
	
	@XmlGetter( element="phone" )
	public String getPhone () {
		return os_phone;
	}
	
	@XmlGetter( element="country" )
	public String getCountry () {
		return os_country;
	}
	
	@XmlGetter( element="snailmail" )
	public String getSnailMail ()
	{
		return os_address;
	}
	
	@XmlGetter( element="usastate" )
	public UsaState getUsaState () {
		return on_state;
	}
	
	@XmlGetter( element="zipcode" )
	public String getZipcode () {
		return os_zip;
	}
	
	@XmlGetter( element="messenger" )
	public String getMessenger () {
		return os_messenger;
	}
	
	@XmlGetter( element="url" )
	public URL getUrl () {
		return ourl_address;
	}
	
	/**
	 * XmlDataSetter utility class to support SimpleXmlData
	 * XML to Object mapping
	 */
	public static class AddressTypeSetter extends SimpleXmlDataSetter {
		public void setData ( Object x_target, String s_data ) throws ParseException {
			AddressType n_address = AddressType.parse ( s_data );
			
			if ( null == n_address ) {
				throw new ParseException ( "Invalide AddressType: " + s_data );
			}
			((Address) x_target).setAddressType ( n_address );
		}
	}
	
	
	@XmlSetter( element="", attribute="address_type", setter=AddressTypeSetter.class )
	public void setAddressType ( AddressType n_address ) {
		on_address = n_address;
	}
	

	@XmlSetter( element="label" )
	public void setLabel ( String s_label ) {
		os_label = s_label;
	}
	
	/**
	 * XmlDataSetter utility class to support SimpleXmlData
	 * XML to Object mapping
	 */
	public static class EmailSetter extends SimpleXmlDataSetter {
		public void setData ( Object x_target, String s_data ) throws ParseException {
			try {
				InternetAddress mail_addr = new InternetAddress ( s_data );
				((Address) x_target).setEmail ( mail_addr );
			} catch ( Exception e ) {
				throw new ParseException ( "Illegal e-mail: " + s_data, e );
			}
		}
	}

	@XmlSetter( element="email", 
                setter=EmailSetter.class 
                )
	public void setEmail ( InternetAddress mail_address ) {
		omail_address = mail_address;
	}
	
	@XmlSetter( element="phone" )
	public void setPhone ( String s_phone ) {
		os_phone = s_phone;
	}
	
	@XmlSetter ( element="country" )
	public void setCountry ( String s_country ) {
		os_country = s_country;
	}
	
	
	/**
	 * XmlDataSetter utility class to support SimpleXmlData
	 * XML to Object mapping
	 */
	public static class UsaStateSetter extends SimpleXmlDataSetter {
		public void setData ( Object x_target, String s_data ) throws ParseException {
			UsaState n_state = UsaState.parse ( s_data );
			
			if ( null == n_state ) {
				throw new ParseException ( "Invalide UsaState: " + s_data );
			}
			((Address) x_target).setUsaState ( n_state );
		}
	}
	
	@XmlSetter( element="usastate", setter=UsaStateSetter.class )
	public void setUsaState ( UsaState n_state ) {
		on_state = n_state;
	}
	
	@XmlSetter( element="messenger" )
	public void setMessenger ( String s_messenger_spec ) {
		os_messenger = s_messenger_spec;
	}
	
	@XmlSetter( element="snailmail" )
	public void setSnailMail ( String s_mail ) {
		os_mail = s_mail;
	}
	
	@XmlSetter( element="zipcode" )
	public void setZipcode ( String s_zip ) {
		os_zip = s_zip;
	}
	
	/**
	 * XmlDataSetter utility class to support SimpleXmlData
	 * XML to Object mapping
	 */
	public static class UrlSetter extends SimpleXmlDataSetter {
		public void setData ( Object x_target, String s_data ) throws ParseException {
			try {
				URL url_address = new URL ( s_data );
				((Address) x_target).setUrl ( url_address );
			} catch ( Exception e ) {
				throw new ParseException ( "Illegal URL: " + s_data, e );
			}
		}
	}
	
	@XmlSetter ( element="url", setter=UrlSetter.class )
	public void setUrl ( URL url_address ) {
		ourl_address = url_address;
	}
	
	/**
	 * Return a simple copy of this object
	 */
	public SimpleAddress clone ()  {
		return (SimpleAddress) super.clone ();
	}	
	
    public void sync ( Asset a_copy_source ) throws InvalidAssetTypeException {
        if ( this == a_copy_source ) {
            return;
        }
        super.sync ( a_copy_source );
        
        SimpleAddress addr_copy_source = (SimpleAddress) a_copy_source;
        on_address = addr_copy_source.on_address;
        os_phone = addr_copy_source.os_phone;
        omail_address = addr_copy_source.omail_address;
        os_address = addr_copy_source.os_address;
        os_messenger = addr_copy_source.os_messenger;
        os_label = addr_copy_source.os_label;
        ourl_address = addr_copy_source.ourl_address;
        os_country = addr_copy_source.os_country;
        on_state = addr_copy_source.on_state;
        os_zip = addr_copy_source.os_zip;
        os_mail = addr_copy_source.os_mail;
    }
	
}
// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

