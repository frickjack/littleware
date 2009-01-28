/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.misc;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;
import littleware.apps.misc.client.SimpleImageManager;
import littleware.apps.misc.client.SimpleThumbManager;

/**
 * Setup littleware.apps.misc package bindings
 */
public class StandardMiscGuice implements Module {

    public void configure(Binder binder) {
        binder.bind( ImageManager.class ).to( SimpleImageManager.class ).in(Scopes.SINGLETON);
        binder.bind( ThumbManager.class ).to( SimpleThumbManager.class ).in(Scopes.SINGLETON);
    }

}
