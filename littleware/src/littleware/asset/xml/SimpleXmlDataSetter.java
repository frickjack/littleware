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
	
	public void init ( Method meth_setter ) {
		ometh_setter = meth_setter;
	}
	

	/** 
	 * Little convenience method for subtypes that want to 
	 * override setData to handle type-conversion
	 */
	protected Method getSetter () { return ometh_setter; }
	
	
	public void setData ( Object x_target, String s_data ) throws ParseException {
		try {
			ometh_setter.invoke ( x_target, s_data );
		} catch ( Exception e ) {
			throw new ParseException ( "Failure to assign " + s_data + " to " + 
									   ometh_setter.getName () + ", caught: " + e , e );
		}
	}
}


// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

