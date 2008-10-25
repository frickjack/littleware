package littleware.asset.xml;

import java.lang.reflect.*;
import java.io.Serializable;

import littleware.base.AssertionFailedException;


/**
 * Little POJO to stuff some data into for XmlIn methods
 */
public class SimpleXmlDataGetter implements XmlDataGetter {
	private Method       ometh_getter = null;
	
	/**
	 * Do nothing constructor
	 */
	public SimpleXmlDataGetter () {}
	
	public void init ( Method meth_getter ) {
		ometh_getter = meth_getter;
	}
	


	public String getData ( Object x_target ) {
		try {
			Object x_data = ometh_getter.invoke ( x_target );
			if ( null == x_data ) {
				return null;
			}
			return x_data.toString ();
		} catch ( RuntimeException e ) {
			throw e;
		} catch ( Exception e ) {
			throw new AssertionFailedException ( "Failure to invoke getter " + 
									   ometh_getter.getName () + ", caught: " + e , e );
		}
	}
}


// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

