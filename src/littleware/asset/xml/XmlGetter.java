package littleware.asset.xml;


import java.lang.annotation.*;

/**
 * Annotation maps a getX() method to an XML element or attribute
 * name and an XmlDataGetter command object which has
 * the logic necessary to convert the result of the getX() to
 * a String ready to be encoded into an XML document.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface XmlGetter { 
	String   element ();
	String   attribute () default "";
	Class<? extends XmlDataGetter>  getter () default SimpleXmlDataGetter.class;
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

