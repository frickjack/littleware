/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.xml;

import org.xml.sax.helpers.*;

import littleware.asset.AssetBuilder;

/**
 * Utility interface for asset-types that store formatted
 * XML data in the getData asset field at save time,
 * but automatically extract that data at asset load time
 * via setData for access by specialized accessors.
 */
public interface XmlAssetBuilder extends AssetBuilder {
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

