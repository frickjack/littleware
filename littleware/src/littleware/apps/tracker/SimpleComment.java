package littleware.apps.tracker;

import java.security.GeneralSecurityException;
import java.rmi.RemoteException;
import java.io.StringReader;
import java.io.IOException;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
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
    
    public String getSummary () {
        return os_summary;
    }
    
    public String getBucketPath () { return OS_BUCKET_PATH; }
    
    public void setSummary ( String s_summary ) throws BaseException {
        if ( s_summary.length () > SimpleAsset.OI_DATA_LIMIT - 200 ) {
            throw new littleware.base.TooBigException ( "Summary data exceeds size limit: " + 
                                        (SimpleAsset.OI_DATA_LIMIT - 200)
                                        );
        }
        os_summary = s_summary;
    }
    
    public SimpleComment clone () {
        return (SimpleComment) super.clone ();
    }
    
    public void sync ( Asset a_other ) throws InvalidAssetTypeException {
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
		public void startElement( String s_namespace,
						          String s_simple, // simple name (localName)
						          String s_qualified, // qualified name
						          Attributes v_attrs )
            throws SAXException
		{
			os_buffer.setLength ( 0 );
		}
        
		
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
	public String    getData () 
	{
		String s_data = "<asset:comment xmlns:asset=\"" 
                        + littleware.asset.pickle.XmlTranslator.OS_ASSET_NAMESPACE
                        + "\">\n";
		s_data += littleware.base.XmlSpecial.encode ( getSummary () );
		s_data += "\n</asset:comment>";
		return s_data;
	}

    public void saveComment ( BucketManager m_bucket, String s_comment
                              )  throws BaseException, GeneralSecurityException,
        AssetException, RemoteException, BucketException, IOException
    {
        this.setValue ( 1 ); // hasComment
        this.sync ( m_bucket.writeToBucket( this, getBucketPath (),
                                            s_comment, "writing comment" ) 
                    );
    }
    
    public void eraseComment ( BucketManager m_bucket
                              )  throws BaseException, GeneralSecurityException,
        AssetException, RemoteException, BucketException, IOException
    {
        this.setValue ( 0 ); // hasComment
        this.sync ( m_bucket.eraseFromBucket( this, getBucketPath (),
                                              "clearing comment" ) 
                    );
    }

    public String getComment ( BucketManager m_bucket 
                               )  throws BaseException, GeneralSecurityException,
        AssetException, RemoteException, BucketException, IOException
    {
        return m_bucket.readTextFromBucket ( this.getObjectId (), getBucketPath () );
    }

    public boolean hasComment () {
        return (getValue () > 0);
    }
    
    public DefaultHandler getSaxDataHandler () {
		return new XmlDataHandler ();
	}
    

}


// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

