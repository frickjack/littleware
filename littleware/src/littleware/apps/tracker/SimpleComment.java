/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.tracker;

import java.security.GeneralSecurityException;
import java.rmi.RemoteException;
import java.io.StringReader;
import java.io.IOException;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;

import littleware.base.BaseException;
import littleware.base.ParseException;
import littleware.asset.*;
import littleware.asset.xml.*;
import littleware.apps.filebucket.*;


/**
 * Simple implementation of Comment interface.
 */
public class SimpleComment extends SimpleAsset implements Comment, XmlDataAsset {
    private static final String  OS_BUCKET_PATH = "comment.txt";
    private String  os_summary = null;
    
    /** Do nothing constructor */
    public SimpleComment () {
        setAssetType ( TrackerAssetType.COMMENT );
    }
    
    @Override
    public String getSummary () {
        return os_summary;
    }
    
    @Override
    public String getBucketPath () { return OS_BUCKET_PATH; }

    @Override
    public int getMaxSummary() {
        return SimpleAsset.OI_DATA_LIMIT - 200;
    }

    @Override
    public void setSummary ( String s_summary ) {
        if ( s_summary.length () > getMaxSummary() ) {
            throw new IllegalArgumentException ( "Summary data exceeds size limit: " +
                                        (SimpleAsset.OI_DATA_LIMIT - 200)
                                        );
        }
        os_summary = s_summary;
    }
    
    @Override
    public SimpleComment clone () {
        return (SimpleComment) super.clone ();
    }
    
    @Override
    public void sync ( Asset a_other ) {
        if ( this == a_other ) {
            return;
        } 
        super.sync ( a_other );
        os_summary = ((SimpleComment) a_other).os_summary;
    }
    
    /**
     * SAX parser handler 
	 */
	private static class XmlDataHandler extends DefaultHandler {
        public StringBuilder  os_buffer = new StringBuilder ();
        
		public XmlDataHandler () {}
		
		/**
         * Callback for XML start-tag
		 */
        @Override
		public void startElement( String s_namespace,
						          String s_simple, // simple name (localName)
						          String s_qualified, // qualified name
						          Attributes v_attrs )
            throws SAXException
		{
			os_buffer.setLength ( 0 );
		}
        
		
        @Override
		public void characters(char buf[], int offset, int len)
            throws SAXException
        {
            os_buffer.append ( buf, offset, len );
        }
        
        public String getSummary () { return os_buffer.toString (); }
	}
    
    
    /**
     * Parse XML data to determine data summary
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
			os_summary = sax_handler.getSummary ();
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
		String s_data = "<asset:comment xmlns:asset=\"" 
                        + littleware.asset.pickle.XmlTranslator.OS_ASSET_NAMESPACE
                        + "\">\n";
		s_data += littleware.base.XmlSpecial.encode ( getSummary () );
		s_data += "\n</asset:comment>";
		return s_data;
	}

    @Override
    public void saveComment ( BucketManager m_bucket, String s_comment
                              )  throws BaseException, GeneralSecurityException,
        AssetException, RemoteException, BucketException, IOException
    {
        this.setValue ( 1 ); // hasComment
        this.sync ( m_bucket.writeToBucket( this, getBucketPath (),
                                            s_comment, "writing comment" ) 
                    );
    }
    
    @Override
    public void eraseComment ( BucketManager m_bucket
                              )  throws BaseException, GeneralSecurityException,
        AssetException, RemoteException, BucketException, IOException
    {
        this.setValue ( 0 ); // hasComment
        this.sync ( m_bucket.eraseFromBucket( this, getBucketPath (),
                                              "clearing comment" ) 
                    );
    }

    @Override
    public String getComment ( BucketManager m_bucket 
                               )  throws BaseException, GeneralSecurityException,
        AssetException, RemoteException, BucketException, IOException
    {
        return m_bucket.readTextFromBucket ( this.getObjectId (), getBucketPath () );
    }

    @Override
    public boolean hasComment () {
        return (getValue () > 0);
    }
    
    @Override
    public DefaultHandler getSaxDataHandler () {
		return new XmlDataHandler ();
	}
    

}


