/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.security.auth.client;

import littleware.asset.client.LittleService;
import littleware.security.auth.SessionHelper;

/**
 * Marker interface extends SessionHelper with client-side LittleService methods
 */
public interface SessionHelperService extends SessionHelper, LittleService {

}
