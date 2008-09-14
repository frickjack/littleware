package littleware.asset.xml;

import org.xml.sax.helpers.*;

import littleware.asset.Asset; 

/**
 * Utility interface for asset-types that store formatted
 * XML data in the getData asset field at save time,
 * but automatically extract that data at asset load time
 * via setData for access by specialized accessors.
 */
public interface XmlDataAsset extends Asset {
	/**
	 * Hook to get the SAX Handler that
	 * can handle parsing the XML data returned
	 * by getData.
	 *
	 * @return DefaultHandler that can handle
	 *     callbacks from a SAX parser scanning
	 *     the getData() XML string.
	 */
	public DefaultHandler getSaxDataHandler ();
}
// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

