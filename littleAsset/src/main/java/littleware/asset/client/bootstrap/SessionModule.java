/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.client.bootstrap;

import com.google.inject.Module;

/**
 * Bootstrap module for session-scoped classes
 * instantiated by the child-injector at
 * ClientBoootstrap.startSession() ...
 */
public interface SessionModule extends Module {
}
