/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.bootstrap;

import com.google.inject.Module;
import littleware.base.Option;

/**
 * Bootstrap module for session-scoped classes
 * instantiated by the child-injector at
 * ClientBoootstrap.startSession() ...
 */
public interface SessionModule extends Module {
    /**
     * Return an optional class to inject and run after session injector initialization -
     * note the run() method should runs inline with session startup,
     * so it should return quickly or it will lock up session startup
     */
    public Option<? extends Class<? extends Runnable>>  getSessionStarter();    
    
}
