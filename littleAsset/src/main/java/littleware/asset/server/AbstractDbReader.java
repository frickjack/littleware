package littleware.asset.server;

import com.google.inject.Provider;
import java.sql.SQLException;

import littleware.db.DbSimpleReader;

/**
 * Specialization of DbSimpleReader that pulls
 * its db connection from TransactionManager.getConnection.
 */
public abstract class AbstractDbReader<T, R> extends DbSimpleReader<T, R> {

    private final Provider<JdbcTransaction> oprovideTrans;

    /** Constructor calls through to super */
    public AbstractDbReader(String s_query, boolean b_is_function, Provider<JdbcTransaction> provideTrans) {
        super(s_query, b_is_function);
        oprovideTrans = provideTrans;
    }

    @Override
    public T loadObject(R x_arg) throws SQLException {
        JdbcTransaction trans_me = oprovideTrans.get();
        trans_me.startDbAccess();
        try {
            return loadObject(trans_me.getConnection(),
                    x_arg);
        } finally {
            trans_me.endDbAccess();
        }
    }
}
// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

