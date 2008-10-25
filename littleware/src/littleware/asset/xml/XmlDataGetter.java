package littleware.asset.xml;

import java.lang.reflect.Method;

/**
 * Interface for handler that extracts data out
 * of a POJO, and converts it to a string
 * for inclusion in a pickle stream.
 * The XmlGetter Annotation pairs a class getter method
 * with an XmlDataGetter command class and an XML element name.
 * An XML pickler can scan a given object&apos;s annotations
 * to construct a collection of XmlDataGetter objects by
 * which the pickler may serialize the object to an XML string.
 * A littleware Asset implementation implements the XmlDataAsset interface
 * to communicate to XML handlers that it is annotated with
 * XmlGetter and XmlSetter attributes to manage the conversion
 * of the asset getData() blob between XML string and object
 * representation.
 */
public interface XmlDataGetter {
	
	/**
	 * Initialize the getter with the getter Method
	 *
	 * @param meth_getter getter method
	 */
	public void init ( Method meth_getter );
	
	/**
	 * Invoke the getter and return the result converted to String
	 *
	 * @param x_target to invoke getter method against
	 */
	public String getData ( Object x_target );
}


// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

