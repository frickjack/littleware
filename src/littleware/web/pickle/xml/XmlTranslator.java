package littleware.web.pickle.xml;

import java.io.*;
import java.security.GeneralSecurityException;
import org.xml.sax.helpers.DefaultHandler;

import littleware.web.pickle.*;
import littleware.base.*;
import littleware.asset.*;


/** 
 * Specialization of PickleMaker interface for SAX XML pickling.
 * Base class for PickleType.XML PickleMakers.
 */
public abstract class XmlTranslator<T> extends DefaultHandler implements PickleMaker<T> {
	public final static String OS_ASSET_NAMESPACE = 
			"http://www.littleware.com/xml/namespace/2006/asset";
		
	
	/**
	 * Get the stashed result of the XML processing
	 * performed by this handler.
	 * Intended for internal use only - in support
	 * of handler chaining.
	 *
	 * @exception IllegalStateException if somebody tries
	 *         to retrieve a result before it is ready.
	 */
	public abstract T   getResult () throws IllegalStateException;
	
	
	public abstract T unpickle ( Reader io_data ) throws AssetException, BaseException,
		GeneralSecurityException, IOException;
	
	/**
	 * An asset should pickle like this: <br />
	 *     &lt;type-specific wrapper   asset_type=&quot;type-name&quot;&gt; <br />
	 *         ... <br />
	 *         &lt;asset:core&gt; <br />
	 *             littleware.asset.Asset core data
	 *         &lt;/asset:core&gt; <br />
	 *         ... <br />
	 *     &lt;/type-specific wrapper&gt; <br />
	 *
	 *  <p>
	 *  The XMLTranslater requires that the type-specific wrapper contain the asset_type 
	 *  attribute, so that the AssetType.UNKNOWN
	 *  PickleMaker can figure out which specific AssetType is being unpickled
	 *  early in the unpickle process, and hand off parser responsibilities
	 *  to the appropriate PickleMaker.
	 *  The type-specific wrapper allows us to setup context sensitive XSLT
	 *  translation rules, but should not contain any data of consequence.
	 * </p>
	 */
	public abstract void pickle ( T x_in, Writer io_data ) throws AssetException, BaseException, 
		GeneralSecurityException, IOException;
	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

