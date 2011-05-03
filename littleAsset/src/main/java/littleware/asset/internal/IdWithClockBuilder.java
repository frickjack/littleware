/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.internal;

import java.io.Serializable;
import java.util.UUID;
import littleware.asset.IdWithClock;
import littleware.base.Maybe;
import littleware.base.Option;

/**
 * Basic implementation of IdWithClock.Builder
 */
public class IdWithClockBuilder implements IdWithClock.Builder {

    private static class Data implements IdWithClock, Serializable {
        private UUID   id;
        private long   transaction;
        private Option<UUID> maybeFrom = Maybe.empty();


        /** Empty constructor for serialization */
        public Data() {}

        /** Inject read-only properties, from may be null */
        public Data( UUID id, UUID from, long transaction ) {
            this.id = id;
            this.transaction = transaction;
            this.maybeFrom = Maybe.emptyIfNull( from );
        }

        @Override
        public UUID getId() {
            return id;
        }

        @Override
        public Option<UUID> getParentId() {
            return maybeFrom;
        }

        @Override
        public long getTimestamp() {
            return transaction;
        }

    }

    @Override
    public IdWithClock build(UUID id, long transaction) {
        return build( id, null, transaction );
    }

    @Override
    public IdWithClock build(UUID id, UUID from, long transaction) {
        return new Data( id, from, transaction );
    }
}
