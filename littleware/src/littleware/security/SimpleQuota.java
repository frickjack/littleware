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

import java.rmi.RemoteException;
import java.util.UUID;
import java.security.GeneralSecurityException;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import java.io.StringReader;

import littleware.asset.*;
import littleware.asset.xml.XmlDataAsset;
import littleware.base.*;


/**
 * Quota asset gets attached to a user to restrict
 * the user's access to the littleware database in some way.
 */
public class SimpleQuota extends SimpleAsset implements Quota, XmlDataAsset {
	private final static String os_xml_namespace = 
			"http://www.littleware.com/xml/namespace/2006/quota";
    private static final long serialVersionUID = -502948639955038504L;
	
	private int  oi_limit = -1;
	
	public SimpleQuota () {
		super ();
		this.setAssetType ( SecurityAssetType.QUOTA );
	}
	
	
    @Override
	public int getQuotaCount ()
	{
		return (int) getValue ().floatValue ();
	}
	
    @Override
	public void setQuotaCount ( int i_value ) {
		setValue ( (float) i_value );
	}
	

    @Override
	public int getQuotaLimit ()
	{
		return oi_limit;
	}
	
    @Override
	public void setQuotaLimit ( int i_limit )
	{
		oi_limit = i_limit;
	}
	
	
    @Override
	public void incrementQuotaCount () {
		this.setQuotaCount ( this.getQuotaCount () + 1 );
	}
	
	/**
	 * SAX parser handler 
	 */
	private static class XmlDataHandler extends DefaultHandler {
		private int oi_parse_limit = 0;
		
		/**
		 * Callback for XML start-tag.
		 * Pulls the limit attribute out of the quotaspec tag.
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
			if ( s_simple.equals ( "quotaspec" ) ) {
				oi_parse_limit = Integer.parseInt ( v_attrs.getValue ( "limit" ) );
			}
		}
		
		/** Return the quota-limit parsed out of the XML file */
		public int getParseLimit () { return oi_parse_limit; }
		
	}
	

	/**
	 * Parse XML data to determine quota limit and supporting info
	 */
    @Override
	public void        setData ( String s_data ) throws ParseException
	{
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware ( true );
			SAXParser sax_parser = factory.newSAXParser();
			XmlDataHandler  sax_handler = new XmlDataHandler ();
			
			sax_parser.parse( new InputSource ( new StringReader ( s_data ) ), 
							  sax_handler 
							  );
			oi_limit = sax_handler.getParseLimit ();
		} catch ( RuntimeException e ) {
			throw e;
		} catch ( Exception e ) {
			throw new ParseException ( "Failed to parse: " + e, e );
		}
	}
	
	/**
	 * Procedurally generate XML data
	 */
    @Override
	public String    getData () 
	{
		String s_data = "<quota:quotaset xmlns:quota=\"http://www.littleware.com/xml/namespace/2006/quota\">\n";
		s_data += "<quota:quotaspec type=\"update\" limit=\"";
		s_data += Integer.toString ( oi_limit );
		s_data += "\" />\n</quota:quotaset>";
		return s_data;
	}
	
    @Override
	public UUID getNextInChainId ()
	{
		return getToId ();
	}

    @Override
	public Quota getNextInChain ( AssetRetriever m_retriever 
								  ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		return (Quota) getToAsset ( m_retriever );
	}
	

    @Override
	public UUID getUserId () 	{
	    return getFromId ();	
	}
	
    @Override
	public LittleUser getUser ( AssetRetriever m_retriever ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
	    return getFromAsset ( m_retriever ).narrow(LittleUser.class);
	}
	
    @Override
	public DefaultHandler getSaxDataHandler () {
		return new XmlDataHandler ();
	}
	
	/**
	 * Return a simple copy of this object
	 */
    @Override
	public SimpleQuota clone ()  {
		return (SimpleQuota) super.clone ();
	}	
    
    @Override
    public void sync ( Asset a_copy_source ) throws InvalidAssetTypeException {
        if ( this == a_copy_source ) {
            return;
        }
        super.sync ( a_copy_source );
        
        oi_limit = ((SimpleQuota) a_copy_source).oi_limit;
    }
    
}

