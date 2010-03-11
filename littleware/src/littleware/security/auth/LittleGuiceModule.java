/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.security.auth;

import com.google.inject.Module;


/**
 * Most of the classes that a client interacts with have
 * a dependency on an injected SessionHelper acquired
 * by the user authentication process.  This interface
 * extends the GUICE  Module to include a SessionHelper
 * property that a client may set with an authenticated
 * module before configuring a GUICE injector.
 * In this way we can setup a workflow where
 * a client follows a procedure like this:
 *     <ol>
 *     <li> Load a LittleGuiceModule implementation via
 *               Class.forName( ... ).newInstance()
 *        </li>
 *     <li> Initialize the SessionHelper property </li>
 *     <li> Setup a GUICE injector to build up the rest of the
 *               client side objects </li>
 *     </ol>
 */
public interface LittleGuiceModule extends Module {
    public SessionHelper getSessionHelper ();
    public void setSessionHelper( SessionHelper helper );
}
