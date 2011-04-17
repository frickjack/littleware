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

import java.lang.reflect.Method;

import littleware.base.ParseException;


/**
 * Interface for handler that takes string data 
 * (usually from a pickle stream), and assigns
 * that data to an object via an annotated Setter
 * method after converting the String to the
 * appropriate type for the setter.
 * The XmlSetter Annotation pairs a class setter method
 * with an XmlDataSetter command class and an XML element name.
 * A SAX XML parser can scan a given object&apos;s annotations
 * to construct a collection of XmlDataSetter objects by
 * which the parser may assign data from the XML stream
 * to the object under construction. 
 */
public interface XmlDataSetter {
	/**
 	 * Initialize the getter with the getter Method
	 *
	 * @param meth_getter getter method
	 */
	public void init ( Method meth_getter );
	
	
	/**
	 * Invoke the setter after applying the arg-parser (if necessary)
	 * on the given String data pulled out of the XML
	 *
	 * @param x_target to invoke method against
	 * @param s_data from XML
	 * @throws ParseException on failure to assign the data via the given setter
	 */
	public void setData ( Object x_target, String s_data ) throws ParseException;
}

