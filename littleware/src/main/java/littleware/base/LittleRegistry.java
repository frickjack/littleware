/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.base;

/**
 * Interface maps service-name to service-handler.
 * Leverage at bootstrap time to map asset-types to
 * asset-specializers, service-types to RMI services, etc.
 */
public interface LittleRegistry<K,V> {
    public V  getService( K name );
    public void registerService( K name, V service );
}
