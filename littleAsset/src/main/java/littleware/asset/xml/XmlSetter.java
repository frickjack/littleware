package littleware.asset.xml;


import java.lang.annotation.*;

/**
 * Annotation maps a setX() method to an XML element or attribute
 * name and an XmlDataSetter command object which has
 * the logic necessary to accept the String data that
 * an XML parser extracts from the named XML element,
 * convert the String as necessary, and invoke the setX() 
 * method to populate the object with data from the XML stream.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface XmlSetter { 
	String   element ();
	String   attribute () default "";
	Class<? extends XmlDataSetter>  setter () default SimpleXmlDataSetter.class;
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

