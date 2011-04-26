/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.net;

import littleware.base.Maybe;
import littleware.base.cache.CacheableObject;
import littleware.base.event.LittleTool;

/**
 * Reference to a (usually immutable) object.
 */
public interface LittleReference<T extends CacheableObject> extends LittleTool {
    public T getReference();
    public void setReference( T value );
}
