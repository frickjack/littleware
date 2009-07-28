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

/**
 * Basic implementation of IdWithClock.Builder
 */
public class IdWithClockBuilder implements IdWithClock.Builder {

    private static class Data implements IdWithClock, Serializable {
        private UUID   id;
        private long   transaction;

        public Data( UUID id, long transaction ) {
            this.id = id;
            this.transaction = transaction;
        }

        @Override
        public UUID getId() {
            return id;
        }

        @Override
        public long getTransaction() {
            return transaction;
        }

    }


    @Override
    public IdWithClock build(UUID id, long transaction) {
        return new Data( id, transaction );
    }

}
