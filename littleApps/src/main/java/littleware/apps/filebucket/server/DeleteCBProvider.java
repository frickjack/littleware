/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.filebucket.server;

import littleware.asset.Asset;

/**
 * Factory interface provides runnable to execute after
 * an asset delete transaction commits successfully to cleanup
 * any bucket data associated with the deleted asset.
 * May need to soup this AssetSpecializer and transaction system
 * later to allow other cross-asset decorators, but we'll just
 * hook in the Bucket cleanup as a special case for now.
 */
public interface DeleteCBProvider {
    public Runnable build( Asset aDelete );
}
