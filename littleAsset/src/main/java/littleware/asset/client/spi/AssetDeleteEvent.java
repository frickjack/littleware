/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.client.spi;

import java.util.UUID;

/**
 *
 * @author pasquini
 */
public class AssetDeleteEvent extends LittleServiceEvent {
    private static final long serialVersionUID = -3732208122310806735L;
    private final UUID  deleteId;

    public AssetDeleteEvent( Object source, UUID deleteId ) {
        super( source );
        this.deleteId = deleteId;
    }

    public UUID getDeletedId () {
        return deleteId;
    }
}
