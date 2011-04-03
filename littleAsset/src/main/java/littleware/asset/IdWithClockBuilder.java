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

import java.io.Serializable;
import java.util.UUID;
import littleware.base.Maybe;

/**
 * Basic implementation of IdWithClock.Builder
 */
public class IdWithClockBuilder implements IdWithClock.Builder {

    private static class Data implements IdWithClock, Serializable {
        private UUID   id;
        private long   transaction;
        private Maybe<UUID> maybeFrom = Maybe.empty();


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
        public Maybe<UUID> getFrom() {
            return maybeFrom;
        }

        @Override
        public long getTransaction() {
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
