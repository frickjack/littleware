/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset;

import com.google.inject.ImplementedBy;
import java.util.UUID;
import littleware.base.Maybe;

/**
 * Little POJO returned by AssetSearchManager.checkTransactionLog
 * to help a client keep its cache in sync with the server.
 * Tracks asset id with a transaction count and parent/from id.
 */
public interface IdWithClock {

    /** Factory for IdWithClock objects */
    public interface Builder {
        public IdWithClock build( UUID id, long timestamp );
        /** from may be null */
        public IdWithClock build( UUID id, UUID parent, long timestamp );
    }


    public UUID getId();
    /**
     * From property set if asset with id has a non-null fromId.
     * A client that is not tracking id may still chose to load
     * id if the client is tracking id's parent.
     */
    public Maybe<UUID> getParentId();
    public long getTimestamp();
}
