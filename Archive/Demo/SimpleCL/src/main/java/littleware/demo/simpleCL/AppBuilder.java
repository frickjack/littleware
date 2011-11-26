/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.demo.simpleCL;

import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * Specify argv, then build a callable command
 */
public interface AppBuilder {
    public AppBuilder argv( Collection<String> value );
    public Callable<String> build();
}
