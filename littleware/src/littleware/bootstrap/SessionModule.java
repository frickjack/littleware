/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.bootstrap;

import com.google.inject.Module;

/**
 * Bootstrap module for session-scoped classes
 * instantiated by the child-injector at
 * ClientBoootstrap.startSession() ...
 */
public interface SessionModule extends Module {
    /**
     * Return a class to inject and run after session injector initialization -
     * note the run() method should runs inline with session startup,
     * so it should return quickly or it will lock up session startup
     */
    public Class<? extends Runnable>  getSessionStarter();    
    
    /**
     * Convenience class - do nothing starter
     */
    public static class NullStarter implements Runnable {
        @Override
        public void run() {}
    }
}
