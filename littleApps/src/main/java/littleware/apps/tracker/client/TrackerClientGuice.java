/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.tracker.client;

import com.google.inject.Binder;
import com.google.inject.Module;
import littleware.apps.tracker.TaskManagerRemote;

/**
 * Force load of TaskManagerRemote so SERVICE_HANDLE gets registered,
 * bind TaskManagerRemote to TaskManagerRemoteService
 */
public class TrackerClientGuice implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind( TaskManagerRemote.class ).to( TaskManagerRemoteService.class );
    }

}
