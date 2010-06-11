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

import com.google.inject.Singleton;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Do-nothing BundleActivator
 */
@Singleton
public final class NullActivator implements BundleActivator {

    @Override
    public void start(BundleContext bc) throws Exception {
    }

    @Override
    public void stop(BundleContext bc) throws Exception {
    }

}
