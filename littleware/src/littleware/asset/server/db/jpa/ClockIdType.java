/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.server.db.jpa;

/**
 * JPA query support type for DbLogLoader
 */
public class ClockIdType {
    private final String id;
    private final long transaction;
    private final String fromId;

    public String getFromId() {
        return fromId;
    }

    public String getId() {
        return id;
    }

    public long getTransaction() {
        return transaction;
    }


    public ClockIdType( String id, String fromId, long transaction ) {
        this.id = id;
        this.transaction = transaction;
        this.fromId = fromId;
    }


}
