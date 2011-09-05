/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.bootstrap;

import littleware.bootstrap.AppBootstrap.AppProfile;

public interface SessionModuleFactory {
    public SessionModule build(AppProfile profile);
}
