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

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Homebrew transaction counter.  Increment this within a transaction block.
 */
@Entity(name="customTransaction")
@Table( name="littleTran", schema="littleware" )
public class TransactionEntity implements Serializable {
    private static final long serialVersionUID = -7258744014535464437L;
    private long olTransaction = 0;
    private int  oiId = 0;

    @Id
    @Column( name="i_id")
    public int getId() {
        return oiId;
    }
    public void setId( int iId ) {
        oiId = iId;
    }

    @Column( name="l_transaction")
    public long getTransaction() {
        return olTransaction;
    }
    public void setTransaction ( long lTransaction ) {
        olTransaction = lTransaction;
    }
}
