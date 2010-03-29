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

import java.util.Date;
import java.util.UUID;
import littleware.apps.tracker.Comment.CommentBuilder;
import littleware.base.CacheableObject;
import littleware.base.Maybe;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import littleware.asset.*;
import littleware.asset.xml.*;


/**
 * Simple implementation of Comment interface.
 */
public class SimpleComment extends SimpleAssetBuilder implements Comment.CommentBuilder {
    private static final String  OS_BUCKET_PATH = "comment.txt";
    private String  os_summary = null;
    
    /** Do nothing constructor */
    public SimpleComment () {
        super ( TrackerAssetType.COMMENT );
    }
    
    
    public String getSummary () {
        return os_summary;
    }

    
    public String getFullText() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
    public String getStateString() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
    public Maybe<UUID> getLink(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
    public Maybe<Date> getDate(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
    public Maybe<String> getAttribute(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }



    @Override
    public CommentBuilder copy(Asset source) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CommentBuilder parent(Asset value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setSummary(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CommentBuilder summary(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setFullText(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CommentBuilder fullText(String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Comment build() {
        throw new UnsupportedOperationException("Not supported yet.");
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
    

        /*..
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
*/

    
}


