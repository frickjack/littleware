/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.base;

import java.util.HashMap;
import java.util.Map;

/**
 * Straight forward implementation of LittleRegistry
 */
public class SimpleLittleRegistry<K,V> implements LittleRegistry<K,V> {
    private final Map<K,V> omap = new HashMap<K,V> ();

    @Override
    public V getService(K name) {
        return omap.get( name );
    }

    @Override
    public void registerService(K name, V service) {
        omap.put( name, service );
    }

}
