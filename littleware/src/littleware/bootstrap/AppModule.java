/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.bootstrap;

import com.google.inject.Module;
import littleware.base.Maybe;
import littleware.bootstrap.AppBootstrap.AppConfig;
import org.osgi.framework.BundleActivator;

/**
 * Bootstrap module for application-mode bootstrap.
 */
public interface AppModule extends Module {
    public AppConfig                               getConfig();
    public Maybe<Class<? extends BundleActivator>> getActivator();
    
    public interface Factory {
        public AppModule build( AppConfig config );
    }
}
