/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.security;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.security.Principal;
import java.security.acl.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import java.io.StringReader;

import littleware.asset.*;
import littleware.asset.xml.XmlDataAsset;
import littleware.base.*;


/**
 * Simple implementation of the LittleAclEntry interface.
 * Overrides setData/getData to extract XML permission data.
 */
class SimpleAclEntry extends SimpleAsset implements LittleAclEntry, XmlDataAsset {
	private static   Logger        olog_generic = Logger.getLogger ( "littleware.security.SimpleAclEntry" );
    private static final long serialVersionUID = -5342316532664742997L;
	
	private boolean                ob_readonly = false;
	private Set<Permission>		   ov_permissions = new HashSet<Permission> ();
	private LittlePrincipal        op_principal = null;
	
	/** Do nothing default constructor */
	public SimpleAclEntry () {
		setValue ( 1 );
		setAssetType ( SecurityAssetType.ACL_ENTRY );
	}
	
	/**
	 * Constructor associates a Principal with this entry,
     * and initializes the Asset name to p_principal.getName ()
	 *
	 * @param p_principal
	 */
	public SimpleAclEntry ( Principal p_principal ) {
		setValue ( 1 );
		setAssetType ( SecurityAssetType.ACL_ENTRY );
		setPrincipal ( p_principal );
	}
	
	/**
	 * Procedurally generate the data-string based on the permissions associated
	 * with this entry
	 */
    @Override
	public String      getData ()
	{
		StringBuilder s_data = new StringBuilder ();
		s_data.append ( "<acl:permlist xmlns:acl=\"http://www.littleware.com/xml/namespace/2006/acl\">\n" );
		for ( Permission perm_entry : ov_permissions ) {
			s_data.append ( "<acl:perm>" );
			s_data.append ( UUIDFactory.makeCleanString ( ((LittlePermission) perm_entry).getObjectId () ) );
			s_data.append ( "</acl:perm>\n" );
		}
		s_data.append ( "</acl:permlist>\n" );
		return s_data.toString ();
	}
	
	
	/**
	 * SAX parser handler 
	 */
	private static class XmlDataHandler extends DefaultHandler {
		private String           os_data = "";
		private boolean          ob_getdata = false;
		private Set<Permission>  ov_parse_perms = new HashSet<Permission> ();
		
		
		/**
		 * Callback for XML start-tag
		 */
        @Override
		public void startElement(String s_namespace,
								 String s_simple, // simple name (localName)
								 String s_qualified, // qualified name
								 Attributes v_attrs )
			throws SAXException
		{
			// Clear the data
			/*..
			olog_generic.log ( Level.FINE, "Starting element: " + s_simple + 
							   ", " + s_qualified
							   );
			..*/
			if ( s_simple.equals ( "perm" ) ) {
				os_data = "";
				ob_getdata = true;
			}
		}
		
		/**
		 * Callback for XML end-element
		 *
		 * @param s_simple name of element
		 * @param s_qualified name of element
		 */
        @Override
		public void endElement(String s_namespace,
							   String s_simple, 
							   String s_qualified 
							   )
		throws SAXException
		{
			if ( ob_getdata ) {
				String s_uuid = os_data.trim ();

				ob_getdata = false;

				try {
					UUID   u_perm = UUIDFactory.parseUUID ( s_uuid );
					ov_parse_perms.add ( LittlePermission.getMember ( u_perm ) );
				} catch ( IllegalArgumentException e ) {
					throw new SAXException ( "Invalid UUID: " + s_uuid, e );
				} catch ( NoSuchThingException e ) {
					throw new SAXException ( "Invalid UUID: " + s_uuid, e );
				}
			}
		}
		
        @Override
		public void characters(char buf[], int offset, int len)
			throws SAXException
		{
			if ( ob_getdata ) {
				String s_in = new String(buf, offset, len);
				os_data += s_in;
			}
		}
		
		/**
		 * Once parsing is complete - the set of permissions associated with this entry
		 *   should be ready to go
		 */
		public Set<Permission> getPermissions () { return ov_parse_perms; }
	}
	
	/**
     * Assign values to this entry's permission set based
	 * on the supplied data
	 *
	 * @param ParseException if data not formatted correctly
	 */
    @Override
	public void setData ( String s_data ) throws ParseException {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware ( true );
			SAXParser sax_parser = factory.newSAXParser();
			XmlDataHandler  sax_handler = new XmlDataHandler ();
			
			sax_parser.parse( new InputSource ( new StringReader ( s_data ) ), 
							  sax_handler 
							  );
			ov_permissions = sax_handler.getPermissions ();
		} catch ( RuntimeException e ) {
			throw e;
		} catch ( Exception e ) {
			throw new ParseException ( "Failed to parse: " + e, e );
		}
	}
	

	
	/**
	 * Set this entry read-only.  Once set cannot be undone - have to  clone
	 * to get modifiable copy.
	 */
    @Override
	public final void setReadOnly () {
		ob_readonly = true;
	}
    
    @Override
    public boolean isReadOnly () {
        return ob_readonly;
    }
	
	/** Return true if this entry has been set read-only */
	public final boolean getReadOnly () { return ob_readonly; }
	
	/**
	 * Add the specified permission to this Acl entry
	 *
	 * @param x_permission to add
	 * @return true if permission added, false if already in entry
	 */
    @Override
	public boolean addPermission ( Permission x_permission ) {
		if ( ob_readonly ) {
			throw new ReadOnlyException ();
		}
		return ov_permissions.add ( x_permission );
	}
	
	
	/**
	 * Remove the specified permission from this Acl entry
	 *
	 * @param x_permission to drop
	 * @return true if permission removed, false if not an entry
	 */
    @Override
	public boolean removePermission ( Permission x_permission ) {
		if ( ob_readonly ) {
			throw new ReadOnlyException ();
		}
		return ov_permissions.remove ( x_permission );
	}
	
	
	/**
	 * Check if the specified permission is part of this entry
	 *
	 * @param x_permission to check
	 * @return true if in entry, false otherwise
	 */
    @Override
	public boolean checkPermission ( Permission x_permission ) {
		return ov_permissions.contains ( x_permission );
	}
	
	/**
	 * Return true if this is a negative Acl entry (shortcut to getValue() == 0)
	 */
    @Override
	public boolean isNegative () {
		return (getValue () == 0); 
	}
	
	/**
	 * Get the principal for which this Acl entry tracks permissions
	 */
    @Override
	public LittlePrincipal getPrincipal () {
		return op_principal;
	}
	
	/**
	 * Create a copy of this entry with its own permission-set -
	 *   ready to be specialized.
	 *
	 * @return new SimpleAclEntry instance
	 */
    @Override
	public SimpleAclEntry clone () {
		SimpleAclEntry x_clone = (SimpleAclEntry) super.clone ();
		x_clone.op_principal = op_principal;
		x_clone.ov_permissions = (Set<Permission>) ((HashSet) ov_permissions).clone ();
		return x_clone;
	}
	
    @Override
    public void sync ( Asset a_copy_source )  {
        if ( this == a_copy_source ) {
            return;
        }
        super.sync ( a_copy_source );
        
        SimpleAclEntry  acle_copy_source = (SimpleAclEntry) a_copy_source;
        
        ob_readonly = acle_copy_source.ob_readonly;
        ov_permissions = (Set<Permission>) ((HashSet) acle_copy_source.ov_permissions).clone ();
        op_principal = (LittlePrincipal) ((LittlePrincipal) acle_copy_source.op_principal).clone ();
    }
    
    /**
     * Internal utility - update the asset name based on the
     * assigned principal and negative value.
     * Name gets reset on the server when saved as part of a LittleAcl.
     */
    private void updateName () {
        if ( null == getPrincipal () ) {
            return;
        }
        setName ( getPrincipal ().getName () + "." + (isNegative() ? "negative" : "positive") );
    }
    
	/**
	 * Set this Acl Entry as a negative one - shortcut to setValue(0)
	 */
    @Override
	public void setNegativePermissions () { 
		if ( ob_readonly ) {
			throw new ReadOnlyException ();
		}
        if ( ! isNegative () ) {
            setValue( 0 ); 
            updateName ();
        }
	}
	
	/**
	 * Return an enumeration of the permissions in this entry
	 */
    @Override
	public Enumeration<Permission> permissions () {
		return Collections.enumeration ( ov_permissions );
	}
	
	/**
	 * Set the principal this entry tracks permissions for.
	 * Also resets setToId to the principal id,
     * and sets name to principal-name.
	 *
	 * @param p_principal to track
	 * @return true if principal set, false if principal already assigned
	 */
    @Override
	public boolean setPrincipal ( Principal p_principal ) {
		if ( null == op_principal ) {
			op_principal = (LittlePrincipal) p_principal;
			setToId ( op_principal.getObjectId () );
            updateName ();
			return true;
		}
		return false;
	}
	
	/**
	 * Get a simple String representation of this entry
	 */
    @Override
	public String toString () {
		return "SimpleAclEntry( Principal: " + op_principal + ", permissions: " + ov_permissions + ")";
	}

	/** Enforce read-only constraint, then call through to super */
    @Override
	public void        setValue ( float f_value ) { 
		if ( ob_readonly ) {
			throw new ReadOnlyException ();
		}
		super.setValue ( f_value ); 
	}
	
    @Override
	public DefaultHandler getSaxDataHandler () {
		return new XmlDataHandler ();
	}

}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

