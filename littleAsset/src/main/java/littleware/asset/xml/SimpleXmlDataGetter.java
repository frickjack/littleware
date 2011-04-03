/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

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
	
    @Override
	public void init ( Method meth_getter ) {
		ometh_getter = meth_getter;
	}
	


    @Override
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

