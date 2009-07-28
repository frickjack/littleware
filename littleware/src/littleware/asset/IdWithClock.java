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

/**
 * Little class pairs an asset id with a transaction count.
 */
public interface IdWithClock {

    /** Factory for IdWithClock objects */
    @ImplementedBy(IdWithClockBuilder.class)
    public interface Builder {
        public IdWithClock build( UUID id, long transaction );
    }


    public UUID getId();

    public long getTransaction();
}
