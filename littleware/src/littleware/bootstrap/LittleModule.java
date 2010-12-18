/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.bootstrap;

import com.google.inject.Module;
import org.osgi.framework.BundleActivator;

/**
 * Base littleware module interface defines Guice injection
 * module, and an optional OSGi activator to inject and
 * activate into the littleware runtime.
 */
public interface LittleModule extends Module {
    public Class<? extends BundleActivator> getActivator();
}
