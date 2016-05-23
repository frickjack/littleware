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
 * Base littleware module interface defines Guice injection
 * module, and an optional OSGi activator to inject and
 * activate into the littleware runtime.
 */
public interface LittleModule extends Module {
    
    /**
     * Callback handler interface.
     * Implementations are allocated by the bootstrap runtime via guice injection
     * in arbitrary order, but satisfying guice constraints.
     */
    public interface LifecycleCallback {
        /**
         * Called at startUp time - callbacks to different
         * modules run in arbitrary order
         */
        void startUp();
        /**
         * Called at shutdown time - callbacks run in
         * arbitrary order
         */
        void shutDown();
    }
    
    /**
     * Return an optional class to inject; startUp() runs after all modules
     * have been configured, and shutDown() runs at LittleBootstrap.shutdown time.
     * Note the callback methods run inline with startup and shutdown
     */
    Option<? extends Class<? extends LifecycleCallback>> getCallback();
}
