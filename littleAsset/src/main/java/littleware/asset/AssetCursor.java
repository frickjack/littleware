/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset;

import java.util.Iterator;

/** 
 * Slight specialization of Iterator to allow us to 
 * add some new methods in the future to batch-return methods.
 * Should also extend to support asynchronous load.
 */
public interface AssetCursor<T> extends Iterator<T> {

}
