/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.client;

import java.util.UUID;

/**
 *
 * @author pasquini
 */
public class AssetDeleteEvent extends ServiceEvent {
    private static final long serialVersionUID = -3732208122310806735L;
    private final UUID  ouDelete;

    public AssetDeleteEvent( LittleService source, UUID uDelete ) {
        super( source );
        ouDelete = uDelete;
    }

    public UUID getDeletedId () {
        return ouDelete;
    }
}
