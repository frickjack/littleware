/*
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.db;

import com.google.inject.Provider;
import java.sql.SQLException;
import littleware.asset.server.JdbcTransaction;

import littleware.db.DbSimpleWriter;

/**
 * Specialization of DbSimpleWriter that pulls
 * its db connection from TransactionManager.getConnection.
 */
public abstract class AbstractDbWriter<T> extends DbSimpleWriter<T> {

    private final Provider<JdbcTransaction> oprovideTrans;

    /** Constructor calls through to super */
    public AbstractDbWriter(String s_query, boolean b_is_function, Provider<JdbcTransaction> provideTrans) {
        super(s_query, b_is_function);
        oprovideTrans = provideTrans;
    }

    @Override
    public void saveObject(T x_arg) throws SQLException {
        JdbcTransaction ltrans_me = oprovideTrans.get();
        boolean b_rollback = true;

        ltrans_me.startDbUpdate();
        try {
            saveObject(ltrans_me.getConnection(),
                    x_arg);
            b_rollback = false;
        } finally {
            ltrans_me.endDbUpdate(b_rollback);
        }
    }
}
