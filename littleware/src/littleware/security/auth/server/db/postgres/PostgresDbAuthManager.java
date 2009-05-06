/*
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.security.auth.server.db.postgres;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.*;

import littleware.asset.server.JdbcTransaction;
import littleware.security.auth.server.db.*;
import littleware.db.*;

/**
 * Postgres implementation of DbPasswordManager
 */
public class PostgresDbAuthManager implements DbAuthManager {
    private final Provider<JdbcTransaction>   oprovideTrans;

	/** Do nothing constructor */
    @Inject
	public PostgresDbAuthManager ( Provider<JdbcTransaction> provideTrans ) {
        oprovideTrans = provideTrans;
    }

    @Override
 	public DbWriter<String> makeDbPasswordSaver ( UUID u_user )
	{
		return new DbPasswordSaver ( u_user, oprovideTrans );
	}
	

    @Override
	public DbReader<Boolean,String> makeDbPasswordLoader ( UUID u_user )
	{
		return new DbPasswordLoader ( u_user, oprovideTrans );
	}
}	
