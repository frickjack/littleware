package littleware.security.auth.server.db.postgres;

import java.util.*;

import littleware.security.auth.server.db.*;
import littleware.db.*;

/**
 * Postgres implementation of DbPasswordManager
 */
public class PostgresDbAuthManager implements DbAuthManager {

	/** Do nothing constructor */
	public PostgresDbAuthManager () {}

 	public DbWriter<String> makeDbPasswordSaver ( UUID u_user )
	{
		return new DbPasswordSaver ( u_user );
	}
	

	public DbReader<Boolean,String> makeDbPasswordLoader ( UUID u_user )
	{
		return new DbPasswordLoader ( u_user );
	}
}	
// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

