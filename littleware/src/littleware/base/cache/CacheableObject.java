/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.base.cache;

import java.util.UUID;

/**
 * Cacheable object subtypes have a globally unique
 * object id, and a monotionically increasing
 * timestamp that takes on the value of a
 * globally incrementing counter after every update
 * to the object.  The object-id/transaction-counter
 * can be used by a cacheing system to decide whether
 * a local copy of an object is consistent with the
 * master copy, or to establish the Lamport-time relationship
 * between sets of objects cached on separate systems.
 */
public interface CacheableObject extends Comparable<CacheableObject>, Cloneable, java.io.Serializable {
	
	/**
	 * Get the object id
	 */
	public UUID getId ();
	
	
	/**
	 * Get the transaction count
	 */
	public long getTimestamp ();
}

