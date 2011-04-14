/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.swingbase.controller;

import com.google.inject.ImplementedBy;

/**
 * Implementation manages UI request to shutdown
 */
@ImplementedBy(ConfirmShutdownHandler.class)
public interface ShutdownHandler {
    public void requestShutdown();
}
