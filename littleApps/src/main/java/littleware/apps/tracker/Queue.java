/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.tracker;

import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import java.util.List;

import littleware.base.BaseException;
import littleware.asset.Asset;


/**
 * Interface for Queue assets.
 * Need to rework this as a kind of iterator wrapping a QueueMgr ...
 */
public interface Queue extends Asset {
}

