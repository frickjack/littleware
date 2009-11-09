/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.pickle;

import java.io.Reader;
import java.io.Writer;
import java.io.IOException;
import java.security.GeneralSecurityException;


import littleware.base.*;
import littleware.asset.*;

/**
 * Interface exported by Asset-String translators
 */
public interface PickleMaker<T> {
	/**
	 * Create an Asset from the given string representation
	 * Throws various kinds of exceptions on failure to access
	 * external data that might be needed to assemble the Asset.
	 *
	 * @param reader to read from
	 * @return object unpickled from the stream
	 * @exception littleware.base.ParseException on failure to parse data
	 */
	public T unpickle ( Reader reader ) throws AssetException, BaseException,
	                             GeneralSecurityException, IOException;
	
	/**
	 * Write a string representation of the given asset onto the given Writer.
	 * Throws various exceptions that might result from need to access
	 * external data sources - but should normally not throw an exception.
	 *
	 * @param x object to serialize
	 * @param writer to write asset serialization to
	 */
	public void pickle ( T x, Writer writer ) throws AssetException, BaseException,
		 GeneralSecurityException, IOException;
}

