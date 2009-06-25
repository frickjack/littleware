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

import littleware.base.ParseException;

/**
 * Simple implementation of XmlDataSetter
 */
public class SimpleXmlDataSetter implements XmlDataSetter {
	private Method       ometh_setter = null;
	
	/**
	 * Do nothing constructor
	 */
	public SimpleXmlDataSetter () {}
	
    @Override
	public void init ( Method meth_setter ) {
		ometh_setter = meth_setter;
	}
	

	/** 
	 * Little convenience method for subtypes that want to 
	 * override setData to handle type-conversion
	 */
	protected Method getSetter () { return ometh_setter; }
	
	
    @Override
	public void setData ( Object x_target, String s_data ) throws ParseException {
		try {
			ometh_setter.invoke ( x_target, s_data );
		} catch ( Exception e ) {
			throw new ParseException ( "Failure to assign " + s_data + " to " + 
									   ometh_setter.getName () + ", caught: " + e , e );
		}
	}
}

